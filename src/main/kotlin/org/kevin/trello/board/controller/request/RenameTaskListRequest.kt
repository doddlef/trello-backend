package org.kevin.trello.board.controller.request

data class RenameTaskListRequest(
    val listId: String,
    val newName: String,
)
