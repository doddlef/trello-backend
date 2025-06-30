package org.kevin.trello.board.controller.request

data class MoveTaskRequest(
    val taskId: String,
    val listId: String,
    val afterId: String? = null,
)
