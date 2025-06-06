package org.kevin.trello.account.model

import java.time.LocalDateTime

data class Account(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val nickname: String,
    val password: String?,
    val status: UserStatus,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

    override fun toString(): String {
        return """Account(uid='$uid', email='$email', isEmailVerified=$isEmailVerified, nickname='$nickname', 
            |status=$status, role=$role, createdAt=$createdAt, updatedAt=$updatedAt)""".trimMargin()
    }
}
