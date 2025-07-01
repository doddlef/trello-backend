package org.kevin.trello.board.service

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.service.vo.TaskCreateVO
import org.kevin.trello.board.service.vo.TaskMoveVO
import org.kevin.trello.board.service.vo.TaskUpdateVO
import org.kevin.trello.core.response.ApiResponse

interface TaskService {
    /**
     * Create a new task, or sub-task.
     *
     * @param vo the value object containing task creation details.
     * @return ApiResponse indicating the result of the operation.
     */
    fun createTask(vo: TaskCreateVO): ApiResponse

    /**
     * Move a task to a different list
     *
     * @param vo the value object containing task move details.
     * @return ApiResponse indicating the result of the operation.
     */
    fun moveTask(vo: TaskMoveVO): ApiResponse

    /**
     * Update an existing task.
     *
     * @param vo the value object containing task update details.
     * @return ApiResponse indicating the result of the operation.
     */
    fun updateTask(vo: TaskUpdateVO): ApiResponse

    /**
     * Archive a task.
     *
     * @param taskId the ID of the task to be archived.
     * @param account the account performing the operation.
     * @return ApiResponse indicating the result of the operation.
     */
    fun archiveTask(taskId: String, account: Account): ApiResponse

    // insert sub-task

    // insert label

    // insert attachment
}