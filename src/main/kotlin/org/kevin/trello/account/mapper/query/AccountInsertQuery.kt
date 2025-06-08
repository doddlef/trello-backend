package org.kevin.trello.account.mapper.query

import com.github.f4b6a3.ulid.UlidCreator
import org.kevin.trello.account.model.UserRole
import org.kevin.trello.account.model.UserStatus

data class AccountInsertQuery(
    val email: String,
    val isEmailVerified: Boolean = false,
    val nickname: String,
    /**
     * The password is stored in a hashed format.
     */
    val password: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    val role: UserRole = UserRole.USER,
) {
    val uid = UlidCreator.getMonotonicUlid().toString()
}