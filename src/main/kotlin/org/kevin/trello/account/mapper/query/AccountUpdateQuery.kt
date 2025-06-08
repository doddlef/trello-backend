package org.kevin.trello.account.mapper.query

import org.kevin.trello.account.model.UserRole
import org.kevin.trello.account.model.UserStatus

data class AccountUpdateQuery(
    val email: String? = null,
    val isEmailVerified: Boolean? = null,
    val nickname: String? = null,
    val password: String? = null,
    val status: UserStatus? = null,
    val role: UserRole? = null,
)
