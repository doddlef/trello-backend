package org.kevin.trello.board.service.impl

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.TaskListMapper
import org.kevin.trello.board.mapper.query.TaskListInsertQuery
import org.kevin.trello.board.mapper.query.TaskListUpdateQuery
import org.kevin.trello.board.service.TaskListService
import org.kevin.trello.board.service.vo.TaskListCreateVO
import org.kevin.trello.board.service.vo.TaskListMoveVO
import org.kevin.trello.board.service.vo.TaskListRenameVO
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.exception.NoAuthException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val POSITION_INTERFACE = 1024

@Service
class TaskListServiceImpl(
    private val taskListMapper: TaskListMapper,
    private val boardViewMapper: BoardViewMapper,
): TaskListService {
    private fun validateListName(name: String) {
        if (name.isBlank() || name.length > 255) {
            throw BadArgumentException("Task list name must be between 1 and 255 characters")
        }
    }

    private fun validateModification(account: Account, boardId: String) {
        val boardView = boardViewMapper.findByAccountAndBoard(account.uid, boardId)
            ?: throw BadArgumentException("Given board does not exist")

        if (boardView.readOnly) throw NoAuthException("You do not have permission to modify this board")
    }

    @Transactional
    override fun createNewTaskList(vo: TaskListCreateVO): ApiResponse {
        validateListName(vo.name)
        validateModification(vo.account, vo.boardId)

        val pos = taskListMapper.findByBoard(vo.boardId).let {
            return@let if (it.isNotEmpty()) it.last().position + POSITION_INTERFACE else POSITION_INTERFACE
        }

        val listId = TaskListInsertQuery(
            boardId = vo.boardId,
            name = vo.name,
            position = pos,
            createdBy = vo.account.uid
        ).let {
            taskListMapper.insert(it)
            it.listId
        }

        return ApiResponse.success()
            .message("Task list created successfully")
            .add("listId" to listId)
            .add("position" to pos)
            .build()
    }

    @Transactional
    override fun renameTaskList(vo: TaskListRenameVO): ApiResponse {
        validateListName(vo.newName)

        taskListMapper.findByListId(vo.listId).let {
            if (it == null) {
                throw BadArgumentException("Task list with ID ${vo.listId} does not exist")
            }
            if (it.name == vo.newName) {
                return ApiResponse.success().message("name not change").build()
            }
            validateModification(vo.account, it.boardId)
        }

        TaskListUpdateQuery(
            listId = vo.listId,
            name = vo.newName,
        ).let {
            taskListMapper.updateById(it)

            return ApiResponse.success()
                .message("Task list renamed successfully")
                .add("newName" to vo.newName)
                .build()
        }
    }

    @Transactional
    override fun moveTaskList(vo: TaskListMoveVO): ApiResponse {
        val (listId, afterListId, boardId, account) = vo
        validateModification(account, boardId)

        val lists = taskListMapper.findByBoard(boardId).sortedBy { it.position }
        lists.find { it.listId == listId }
            ?: throw BadArgumentException("Task list with ID $listId does not exist")
        if (afterListId != null && afterListId == listId)
            throw BadArgumentException("Cannot move task list to itself")

        val afterIdx = afterListId?.let { lists.indexOfFirst { l -> l.listId == it }} ?: -1
        val beforeIdx = afterIdx + 1

        val prevPos = if (afterIdx >= 0) lists[afterIdx].position else 0
        val nextPos = if (beforeIdx < lists.size) lists[beforeIdx].position else prevPos + POSITION_INTERFACE
        var newPos = (prevPos + nextPos) / 2

        if (newPos - prevPos <= 1) {
            lists.forEachIndexed { idx, l ->
                val pos = (idx + 1) * POSITION_INTERFACE
                if (l.position != pos) {
                    TaskListUpdateQuery(
                        listId = l.listId,
                        position = pos
                    ).let { taskListMapper.updateById(it) }
                }
                if (l.listId == listId) newPos = pos
            }
        } else {
            TaskListUpdateQuery(
                listId = listId,
                position = newPos
            ).let { taskListMapper.updateById(it) }
        }

        return ApiResponse.success()
            .message("Task list moved successfully")
            .add("newPosition" to newPos)
            .build()
    }

    @Transactional
    override fun archiveTaskList(listId: String, account: Account): ApiResponse {
        val list = taskListMapper.findByListId(listId)
            ?: throw BadArgumentException("Task list with ID $listId does not exist")
        validateModification(account, list.boardId)

        TaskListUpdateQuery(
            listId = listId,
            archived = true
        ).let {
            taskListMapper.updateById(it)
            return ApiResponse.success()
                .message("Task list archived successfully")
                .build()
        }
    }
}