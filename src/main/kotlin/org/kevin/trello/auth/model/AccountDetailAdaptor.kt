package org.kevin.trello.auth.model

import org.kevin.trello.account.model.Account
import org.kevin.trello.account.model.UserStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AccountDetailAdaptor(
    val account: Account,
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_" + account.role))
    }

    override fun getPassword(): String? {
        return account.password
    }

    override fun getUsername(): String {
        return account.email
    }

    override fun isAccountNonLocked(): Boolean =
        account.status == UserStatus.ACTIVE

    override fun isEnabled(): Boolean =
        account.isEmailVerified
}