package org.kevin.trello.board.mapper.query

data class BoardViewSearchQuery(
    val uid: String,
    val boardId: String? = null,
    val startWith: String? = null,
    /** DEFAULT(isFavorite, lastOpen), LAST_OPEN, NAME */
    val orderBy: String? = null,
)
