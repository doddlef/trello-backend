package org.kevin.trello.auth.service.impl

import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.auth.model.AccountDetailAdaptor
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val accountMapper: AccountMapper,
): UserDetailsService {

    /**
     * load user by username(email).
     */
    override fun loadUserByUsername(username: String?): UserDetails {
        if (username.isNullOrBlank()) throw UsernameNotFoundException("email cannot be null or blank")
        val account = accountMapper.findByEmail(username)

        if (account != null) return AccountDetailAdaptor(account)
        throw UsernameNotFoundException("email $username not found")
    }
}