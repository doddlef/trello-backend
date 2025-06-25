package org.kevin.trello.board.mapper.query

import java.time.LocalDateTime

data class BoardOwnershipUpdateQuery(
    val boardId: String,
    val uid: String,
    val readOnly: Boolean? = null,
    val lastOpen: LocalDateTime?= null,
    val isFavorite: Boolean? = null,
)
