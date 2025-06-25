package org.kevin.trello.board.service

import org.kevin.trello.board.service.vo.TaskListCreateVO
import org.kevin.trello.board.service.vo.TaskListMoveVO
import org.kevin.trello.board.service.vo.TaskListRenameVO
import org.kevin.trello.core.response.ApiResponse

interface TaskListService {
    fun createNewTaskList(vo: TaskListCreateVO): ApiResponse
    fun renameTaskList(vo: TaskListRenameVO): ApiResponse
    fun moveTaskList(vo: TaskListMoveVO): ApiResponse
}