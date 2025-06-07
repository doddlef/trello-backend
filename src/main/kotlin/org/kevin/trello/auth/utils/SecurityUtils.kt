package org.kevin.trello.auth.utils

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.model.AccountDetailAdaptor
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun currentAccount(): Account? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        return (authentication.principal as? AccountDetailAdaptor)?.account
    }
}