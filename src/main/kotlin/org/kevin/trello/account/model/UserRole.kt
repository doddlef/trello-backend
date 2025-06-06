package org.kevin.trello.account.model

enum class UserRole(
    val role: String,
) {
    ADMIN("ADMIN"), USER("USER");

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> throw IllegalArgumentException("Unknown UserRole: $value")
            }
        }
    }
}