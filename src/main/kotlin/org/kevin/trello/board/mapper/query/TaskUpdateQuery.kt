package org.kevin.trello.board.mapper.query

import java.time.LocalDate

data class TaskUpdateQuery(
    val taskId: String,

    val listId: String? = null,
    val finished: Boolean? = null,
    val position: Double? = null,
    val title: String? = null,
    val description: String? = null,
    val date: LocalDate? = null,
    val archived: Boolean? = null,
)
