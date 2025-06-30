package org.kevin.trello.board.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Task(
    /**
     * Unique identifier for the task.
     */
    val taskId: String,

    /**
     * Unique identifier for the list to which this task belongs.
     */
    val listId: String,

    /**
     * Indicates whether the task is finished.
     */
    val finished: Boolean,

    /**
     * Position of the task in the list or in parent task, used for ordering.
     */
    val position: Double,

    /**
     * Title of the task.
     */
    val title: String,

    val description: String?,
    val date: LocalDate?,

//    Metadata
    val creatorId: String,
    val archived: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
