package org.kevin.trello.board.mapper.query

import com.github.f4b6a3.ulid.UlidCreator

data class TaskListInsertQuery(
    val boardId: String,
    val name: String,
    val position: Int,
    val createdBy: String,
) {
    val listId = UlidCreator.getMonotonicUlid().toString()
}
