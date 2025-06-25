package org.kevin.trello.board.service.impl

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.TaskListMapper
import org.kevin.trello.board.mapper.query.TaskListInsertQuery
import org.kevin.trello.board.model.BoardView
import org.kevin.trello.board.service.TaskListService
import org.kevin.trello.board.service.vo.TaskListCreateVO
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
    private fun validateCreateRequest(vo: TaskListCreateVO) {
        if (vo.name.isBlank() || vo.name.length > 255) {
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
        validateCreateRequest(vo)
        validateModification(vo.account, vo.boardId)

        val pos = taskListMapper.findByBoard(vo.boardId).let {
            return@let if (it.size > 0) it.first().position + POSITION_INTERFACE else POSITION_INTERFACE
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
}