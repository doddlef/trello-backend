package org.kevin.trello.board.mapper.query

data class TaskListUpdateQuery(
    val listId: String,
    val name: String? = null,
    val position: Int? = null,
)
