package org.kevin.trello.board.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.board.controller.request.CreateTaskListRequest
import org.kevin.trello.board.service.TaskListService
import org.kevin.trello.board.service.vo.TaskListCreateVO
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tasklist")
class TaskListController(
    private val taskListService: TaskListService,
) {
    @PostMapping
    fun createNewTaskList(@RequestBody request: CreateTaskListRequest): ApiResponse {
        val account = SecurityUtils.currentAccount()
            ?: throw TrelloException("User must be logged in to create a task list") as Throwable

        return TaskListCreateVO(
            name = request.name.trim(),
            boardId = request.boardId,
            account = account
        ).let {
            taskListService.createNewTaskList(it)
        }
    }
}