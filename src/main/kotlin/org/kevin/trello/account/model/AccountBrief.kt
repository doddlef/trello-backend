package org.kevin.trello.account.model

class AccountBrief(
    account: Account,
) {
    val uid: String = account.uid
    val email: String = account.email
    val isEmailVerified: Boolean = account.isEmailVerified
    val nickname: String = account.nickname
    val status: UserStatus = account.status
    val role: UserRole = account.role

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountBrief

        return uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

    override fun toString(): String {
        return "AccountBrief(uid='$uid', email='$email', isEmailVerified=$isEmailVerified, nickname='$nickname', status=$status, role=$role)"
    }
}