package org.kevin.trello.board.controller.request

data class CreateTaskRequest(
    val title: String,
    val listId: String,
    val parentId: String? = null
)
