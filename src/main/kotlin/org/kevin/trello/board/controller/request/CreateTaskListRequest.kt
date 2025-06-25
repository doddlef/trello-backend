package org.kevin.trello.board.controller.request

data class CreateTaskListRequest(
    val boardId: String,
    val name: String
)