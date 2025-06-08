package org.kevin.trello.auth.model

import org.kevin.trello.auth.utils.TokenGenerator
import java.time.LocalDateTime

data class RefreshToken(
    val id: Long = 0L,
    val content: String = TokenGenerator.generateToken(48),
    val accountUid: String,
    val expireAt: LocalDateTime,
)