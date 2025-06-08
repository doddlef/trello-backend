package org.kevin.trello.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth")
class AuthProperties {
    var accessCookieName: String = "trello_access_k"
    var accessTokenLifeMinutes: Long = 60
    var refreshCookieName: String = "trello_refresh_k"
    var refreshTokenLifeDays: Long = 7

    lateinit var jwtSecret: String

    var emailActiveTokenLifeHours: Long = 24
}