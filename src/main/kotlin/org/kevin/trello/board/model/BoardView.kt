package org.kevin.trello.board.model

import java.time.LocalDateTime

data class BoardView(
    val boardId: String,
    val name: String,
    val visibility: BoardVisibility,
    val uid: String,
    val readOnly: Boolean,
    val addedAt: LocalDateTime,
    val lastOpen: LocalDateTime,
    val isFavorite: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardView

        if (boardId != other.boardId) return false
        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boardId.hashCode()
        result = 31 * result + uid.hashCode()
        return result
    }
}
