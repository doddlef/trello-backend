package org.kevin.trello.board.mapper.query

data class BoardOwnerShipInsertQuery(
    val boardId: String,
    val uid: String,
    val readOnly: Boolean = true,
    val isFavorite: Boolean = false,
)