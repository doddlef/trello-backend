package org.kevin.trello.board.service

import org.kevin.trello.board.service.vo.TaskCreateVO
import org.kevin.trello.core.response.ApiResponse

interface TaskService {
    /**
     * Create a new task, or sub-task.
     *
     * @param vo the value object containing task creation details.
     * @return ApiResponse indicating the result of the operation.
     */
    fun createTask(vo: TaskCreateVO): ApiResponse

    // move task

    // update title

    // update description

    // update date

    // insert label

    // insert attachment
}