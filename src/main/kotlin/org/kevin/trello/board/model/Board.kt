package org.kevin.trello.board.model

data class Board(
    val boardId: String,
    val name: String,
    val ownerUid: String,
    val visibility: BoardVisibility,
    val deletedFlag: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return boardId == other.boardId
    }

    override fun hashCode(): Int {
        return boardId.hashCode()
    }
}