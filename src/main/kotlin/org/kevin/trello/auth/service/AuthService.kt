package org.kevin.trello.auth.service

import org.kevin.trello.auth.service.vo.EmailPasswordAuthVO
import org.springframework.security.core.Authentication

interface AuthService {
    fun emailPasswordAuthenticate(authVO: EmailPasswordAuthVO): Authentication
}