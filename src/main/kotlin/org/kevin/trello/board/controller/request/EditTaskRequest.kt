package org.kevin.trello.board.controller.request

data class EditTaskRequest(
    val taskId: String,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null, // ISO 8601 format, e.g., "2023-10-01"
)
