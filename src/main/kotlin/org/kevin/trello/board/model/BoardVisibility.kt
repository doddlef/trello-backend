package org.kevin.trello.board.model

enum class BoardVisibility {
    PUBLIC, PRIVATE;

    companion object {
        fun fromString(value: String): BoardVisibility {
            return when (value.uppercase()) {
                "PUBLIC" -> PUBLIC
                "PRIVATE" -> PRIVATE
                else -> throw IllegalArgumentException("Unknown BoardVisibility: $value")
            }
        }
    }
}