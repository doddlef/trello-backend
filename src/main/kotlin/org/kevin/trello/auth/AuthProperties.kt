package org.kevin.trello.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth")
class AuthProperties {
    var accessCookieName: String = "trello_access_k"
    var accessTokenLifeMinutes: Int = 60
    var refreshCookieName: String = "trello_refresh_k"
    var refreshTokenLifeDays: Int = 7

    lateinit var jwtSecret: String
}