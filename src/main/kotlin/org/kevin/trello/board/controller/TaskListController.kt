package org.kevin.trello.board.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.board.controller.request.CreateTaskListRequest
import org.kevin.trello.board.controller.request.MoveTaskListRequest
import org.kevin.trello.board.controller.request.RenameTaskListRequest
import org.kevin.trello.board.service.TaskListService
import org.kevin.trello.board.service.vo.TaskListCreateVO
import org.kevin.trello.board.service.vo.TaskListMoveVO
import org.kevin.trello.board.service.vo.TaskListRenameVO
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @PutMapping("/rename")
    fun renameTaskList(@RequestBody request: RenameTaskListRequest): ApiResponse {
        val account = SecurityUtils.currentAccount()
            ?: throw TrelloException("User must be logged in to rename a task list")

        val (listId, newName) = request
        TaskListRenameVO(
            listId = listId,
            newName = newName.trim(),
            account = account
        ).let {
            return taskListService.renameTaskList(it)
        }
    }

    @PutMapping("/move")
    fun moveTaskList(@RequestBody request: MoveTaskListRequest): ApiResponse {
        val account = SecurityUtils.currentAccount()
            ?: throw TrelloException("User must be logged in to move a task list")

        val (listId, afterListId, boardId) = request
        return taskListService.moveTaskList(
            TaskListMoveVO(
                listId = listId,
                afterListId = afterListId,
                boardId = boardId,
                account = account
            )
        )
    }
}