package org.kevin.trello.auth.controller

import org.kevin.trello.auth.service.RefreshService
import org.kevin.trello.auth.service.RegisterService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenCleaner(
    private val refreshService: RefreshService,
    private val registerService: RegisterService,
) {
    @Scheduled(cron = "0 0 * * * *")
    fun cleanUpExpiredTokens() {
        refreshService.cleanUpExpiredTokens()
        registerService.cleanUpExpiredTokens()
    }
}