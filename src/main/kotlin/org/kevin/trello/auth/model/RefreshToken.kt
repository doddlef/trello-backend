package org.kevin.trello.auth.model

import java.time.LocalDateTime
import java.util.UUID

data class RefreshToken(
    val content: String = UUID.randomUUID().toString(),
    val accountUid: String,
    val expireAt: LocalDateTime,
)