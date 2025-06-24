package org.kevin.trello.board.mapper.query

import com.github.f4b6a3.ulid.UlidCreator
import org.kevin.trello.board.model.BoardVisibility

data class BoardInsertQuery(
    val name: String,
    val ownerUid: String,
    val visibility: BoardVisibility = BoardVisibility.PRIVATE,
    val deletedFlag: Boolean = false,
) {
    val boardId: String = UlidCreator.getMonotonicUlid().toString()
}
