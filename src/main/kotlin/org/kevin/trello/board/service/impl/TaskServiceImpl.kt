package org.kevin.trello.board.service.impl

import org.kevin.trello.board.mapper.TaskMapper
import org.kevin.trello.board.mapper.query.TaskInsertQuery
import org.kevin.trello.board.model.Task
import org.kevin.trello.board.repo.PathHelper
import org.kevin.trello.board.service.TaskService
import org.kevin.trello.board.service.vo.TaskCreateVO
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val POSITION_INTERVAL = 1024

@Service
class TaskServiceImpl(
    private val pathHelper: PathHelper,
    private val taskMapper: TaskMapper,
): TaskService {
    /**
     * check the validity of the TaskCreateVO.
     */
    private fun validateCreateVO(vo: TaskCreateVO) {
        if (vo.title.isBlank()) throw BadArgumentException("Title should not be blank")
        if (vo.title.length > 128) throw BadArgumentException("Title should not exceed 128 characters")
        if (vo.listId.isBlank()) throw BadArgumentException("List ID should not be blank")
        if (vo.parentId != null && vo.parentId.isBlank()) {
            throw BadArgumentException("Parent ID should be blank if provided")
        }

        val (boardView, taskList, task) = if (vo.parentId != null) {
            pathHelper.pathOfTask(vo.parentId, vo.account.uid)
        } else {
            pathHelper.pathOfList(vo.listId, vo.account.uid)
        }

        if (vo.parentId != null) {
            if (task == null || task.archived) {
                throw BadArgumentException("Parent task with ID ${vo.parentId} does not exist")
            }

            if (task.listId != vo.listId) {
                throw BadArgumentException("Parent task with ID ${vo.parentId} does not belong to the list with ID ${vo.listId}")
            }
        }

        if (taskList == null || taskList.archived) {
            throw BadArgumentException("List with ID ${vo.listId} does not exist")
        }
        if (boardView == null) {
            throw BadArgumentException("Board with ID ${taskList.boardId} does not exist")
        }
    }

    @Transactional
    override fun createTask(vo: TaskCreateVO): ApiResponse {
        // validate the vo and authority
        validateCreateVO(vo)



        // create the task
        TaskInsertQuery(
            title = vo.title,
            creatorId = vo.account.uid,
            listId = vo.listId,
            parentId = vo.parentId,
            position = 0,
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
}