package org.kevin.trello.board.controller.request

data class MoveTaskListRequest(
    val listId: String,
    val afterListId: String?,
)
