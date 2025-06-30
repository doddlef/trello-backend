package org.kevin.trello.board.mapper.query

import com.github.f4b6a3.ulid.UlidCreator

data class TaskInsertQuery(
    val title: String,
    val creatorId: String,
    val listId: String,
    val position: Double,
) {
    val taskId = UlidCreator.getMonotonicUlid().toString()
}
