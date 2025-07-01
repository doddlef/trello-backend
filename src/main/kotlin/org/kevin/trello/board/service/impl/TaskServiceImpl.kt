package org.kevin.trello.board.service.impl

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.mapper.TaskMapper
import org.kevin.trello.board.mapper.query.TaskInsertQuery
import org.kevin.trello.board.mapper.query.TaskSearchQuery
import org.kevin.trello.board.mapper.query.TaskUpdateQuery
import org.kevin.trello.board.repo.PathHelper
import org.kevin.trello.board.service.TaskService
import org.kevin.trello.board.service.vo.TaskCreateVO
import org.kevin.trello.board.service.vo.TaskMoveVO
import org.kevin.trello.board.service.vo.TaskUpdateVO
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val POSITION_INTERVAL = 1024.toDouble()

@Service
class TaskServiceImpl(
    private val pathHelper: PathHelper,
    private val taskMapper: TaskMapper,
): TaskService {
    private fun validateTitle(title: String) {
        if (title.isBlank()) throw BadArgumentException("Title should not be blank")
        if (title.length > 128) throw BadArgumentException("Title should not exceed 128 characters")
    }

    private fun validateDescription(description: String) {
        if (description.length > 1024) {
            throw BadArgumentException("Description should not exceed 1024 characters")
        }
    }

    /**
     * check the validity of the TaskCreateVO.
     */
    private fun validateCreateVO(vo: TaskCreateVO) {
        val (title, account, listId ) = vo

        validateTitle(title)
        if (listId.isBlank()) throw BadArgumentException("List ID should not be blank")

        val (boardView, taskList, _) = pathHelper.pathOfList(listId, account.uid)

        if (taskList == null || taskList.archived) {
            throw BadArgumentException("List with ID $listId does not exist")
        }
        if (boardView == null || boardView.readOnly) {
            throw BadArgumentException("Board with ID ${taskList.boardId} does not exist, or you do not have permission to access it")
        }
    }

    @Transactional
    override fun createTask(vo: TaskCreateVO): ApiResponse {
        // validate the vo and authority
        validateCreateVO(vo)

        // create the task
        val pos = TaskSearchQuery(
            listId = vo.listId,
        ).let {
            val tasks = taskMapper.search(it)
            tasks
        }.let {
            return@let if (it.isNotEmpty()) it.last().position + POSITION_INTERVAL else POSITION_INTERVAL
        }

        TaskInsertQuery(
            title = vo.title,
            creatorId = vo.account.uid,
            listId = vo.listId,
            position = pos,
        ).let {
            taskMapper.insert(it)
            val task = taskMapper.findByTaskId(it.taskId)
            if (task == null) {
                throw TrelloException("Failed to create task, please try again")
            }

            return ApiResponse.success()
                .message("Task created successfully")
                .add("task" to task)
                .build()
        }
    }

    private fun validateMoveTarget(taskId: String, uid: String) {
        val (boardView, _, task) = pathHelper.pathOfTask(taskId, uid)
        if (task == null || task.archived) {
            throw BadArgumentException("Task with ID $taskId does not exist")
        }
        if (boardView == null || boardView.readOnly) {
            throw BadArgumentException("board not exist, or you do not have permission to access it")
        }
    }

    private fun validateMoveDestination(listId: String, uid: String) {
        val (boardView, taskList, _) = pathHelper.pathOfList(listId, uid)
        if (taskList == null || taskList.archived) {
            throw BadArgumentException("List with ID $listId does not exist")
        }
        if (boardView == null || boardView.readOnly) {
            throw BadArgumentException("Board with ID ${taskList.boardId} does not exist, or you do not have permission to access it")
        }
    }

    private fun validateMoveVO(vo: TaskMoveVO) {
        val (taskId, listId, _, account) = vo
        if (vo.taskId.isBlank()) throw BadArgumentException("Task ID should not be blank")
        if (vo.listId.isBlank()) throw BadArgumentException("List ID should not be blank")

        validateMoveTarget(taskId, account.uid)
        validateMoveDestination(listId, account.uid)
    }

    private fun decidePosition(taskId: String, listId: String, afterId: String?): Double {
        if (afterId != null && afterId == taskId)
            throw BadArgumentException("Cannot move a task after itself")
        val tasks = taskMapper.search(TaskSearchQuery(listId = listId))

        val afterIdx = afterId?.let { tasks.indexOfFirst { t -> t.taskId == it }} ?: -1
        val beforeIdx = afterIdx + 1

        val prevPos = if (afterIdx >= 0) tasks[afterIdx].position else 0.0
        val nextPos = if (beforeIdx < tasks.size) tasks[beforeIdx].position else prevPos + POSITION_INTERVAL
        var newPos = (prevPos + nextPos) / 2

        if (newPos - prevPos <= 1) {
            tasks.forEachIndexed { idx, t ->
                val pos = (idx + 1) * POSITION_INTERVAL
                if (t.position != pos) {
                    TaskUpdateQuery(
                        taskId = t.taskId,
                        position = pos
                    ).let { taskMapper.updateByTaskId(it) }
                }
                if (t.taskId == taskId) newPos = pos
            }
        }

        return newPos + 0.0
    }

    @Transactional
    override fun moveTask(vo: TaskMoveVO): ApiResponse {
        // validate the vo and authority
        validateMoveVO(vo)

        // decide position
        val newPosition = decidePosition(vo.taskId, vo.listId, vo.afterId)

        // update the task
        TaskUpdateQuery(
            taskId = vo.taskId,
            listId = vo.listId,
            position = newPosition,
        ).let {
            taskMapper.updateByTaskId(it)
        }

        return ApiResponse.success()
            .message("Task moved successfully")
            .add("newPosition" to newPosition)
            .build()
    }

    private fun validateUpdateVO(vo: TaskUpdateVO) {
        val (taskId, title, description, date, account) = vo
        if (taskId.isBlank()) throw BadArgumentException("Task ID should not be blank")

        var changed = false
        if (title != null) {
            validateTitle(title)
            changed = true
        }
        if (description != null) {
            validateDescription(description)
            changed = true
        }
        if (date != null) changed = true
        if (!changed) throw BadArgumentException("No change to update")

        val (boardView, _, task) = pathHelper.pathOfTask(taskId, account.uid)
        if (task == null) throw BadArgumentException("Task with ID $taskId does not exist")
        if (boardView == null || boardView.readOnly)
            throw BadArgumentException("you do not have permission to access the board")
    }

    @Transactional
    override fun updateTask(vo: TaskUpdateVO): ApiResponse {
        validateUpdateVO(vo)

        TaskUpdateQuery(
            taskId = vo.taskId,
            title = vo.title,
            description = vo.description,
            date = vo.date,
        ).let {
            taskMapper.updateByTaskId(it)

            return ApiResponse.success()
                .message("Task updated successfully")
                .build()
        }
    }

    @Transactional
    override fun archiveTask(
        taskId: String,
        account: Account
    ): ApiResponse {
        val (boardView, _, task) = pathHelper.pathOfTask(taskId, account.uid)
        if (task == null) throw BadArgumentException("Task with ID $taskId does not exist")
        if (boardView == null || boardView.readOnly)
            throw BadArgumentException("you do not have permission to access the board")

        TaskUpdateQuery(
            taskId = taskId,
            archived = true,
        ).let {
            taskMapper.updateByTaskId(it)

            return ApiResponse.success()
                .message("Task archived successfully")
                .build()
        }
    }
}