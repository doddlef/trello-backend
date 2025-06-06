package org.kevin.trello.account.model

enum class UserStatus {
    ACTIVE, LOCKED, DELETED;

    companion object {
        fun fromString(value: String): UserStatus {
            return when (value.uppercase()) {
                "ACTIVE" -> ACTIVE
                "LOCKED" -> LOCKED
                "DELETED" -> DELETED
                else -> throw IllegalArgumentException("Unknown UserStatus: $value")
            }
        }
    }
}