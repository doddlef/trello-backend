package org.kevin.trello.board.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.board.controller.request.CreateTaskRequest
import org.kevin.trello.board.controller.request.EditTaskRequest
import org.kevin.trello.board.controller.request.MoveTaskRequest
import org.kevin.trello.board.service.TaskService
import org.kevin.trello.board.service.vo.TaskCreateVO
import org.kevin.trello.board.service.vo.TaskMoveVO
import org.kevin.trello.board.service.vo.TaskUpdateVO
import org.kevin.trello.core.response.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1")
class TaskController(
    private val taskService: TaskService,
) {
    @PostMapping("/task")
    fun createTask(@RequestBody request: CreateTaskRequest): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()

        TaskCreateVO(
            title = request.title,
            listId = request.listId,
            account = account,
        ).let {
            return taskService.createTask(it)
        }
    }

    @PostMapping("/task/move")
    fun moveTask(@RequestBody request: MoveTaskRequest): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()

        TaskMoveVO(
            taskId = request.taskId,
            listId = request.listId,
            afterId = request.afterId,
            account = account,
        ).let {
            return taskService.moveTask(it)
        }
    }

    @PutMapping("/task/edit")
    fun editTask(@RequestBody request: EditTaskRequest): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()
        val (taskId, title, description, date) = request

        TaskUpdateVO(
            taskId = taskId,
            title = title,
            description = description,
            date = date?.let { LocalDate.parse(it) },
            account = account,
        ).let {
            return taskService.updateTask(it)
        }
    }

    @PutMapping("/task/{taskId}/finish")
    fun finishTask(@PathVariable("taskId") taskId: String): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()
        return taskService.finishTask(taskId, account)
    }

    @DeleteMapping("/task/{taskId}")
    fun archiveTask(@PathVariable("taskId") taskId: String): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()
        return taskService.archiveTask(taskId, account)
    }
}