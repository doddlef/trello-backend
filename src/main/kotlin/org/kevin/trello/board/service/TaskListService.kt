package org.kevin.trello.board.service

import org.kevin.trello.board.service.vo.TaskListCreateVO
import org.kevin.trello.core.response.ApiResponse

interface TaskListService {
    fun createNewTaskList(vo: TaskListCreateVO): ApiResponse
}