package org.kevin.trello.auth.model

import java.time.LocalDateTime

data class EmailActiveToken(
    val id: Long,
    val token: String,
    val uid: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)
