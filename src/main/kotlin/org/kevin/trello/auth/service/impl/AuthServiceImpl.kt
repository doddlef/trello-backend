package org.kevin.trello.auth.service.impl

import org.kevin.trello.auth.service.AuthService
import org.kevin.trello.auth.service.vo.EmailPasswordAuthVO
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val authenticationManager: AuthenticationManager,
): AuthService {
    override fun emailPasswordAuthenticate(authVO: EmailPasswordAuthVO): Authentication {
        return authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authVO.email,
                authVO.password
            )
        )
    }
}