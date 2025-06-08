package org.kevin.trello.auth.utils

import java.security.SecureRandom
import java.util.Base64

object TokenGenerator {
    fun generateToken(size: Int = 32): String {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}