package org.kevin.trello.auth.mapper.query

import org.kevin.trello.auth.utils.TokenGenerator
import java.time.LocalDateTime

data class EmailActiveTokenInsertQuery(
    val token: String = TokenGenerator.generateToken(64),
    val uid: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime,
) {
    var id: Long? = null
}
