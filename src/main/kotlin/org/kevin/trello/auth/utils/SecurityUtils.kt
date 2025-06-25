package org.kevin.trello.auth.utils

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.model.AccountDetailAdaptor
import org.kevin.trello.core.exception.TrelloException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun currentAccount(): Account? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        return (authentication.principal as? AccountDetailAdaptor)?.account
    }

    fun currentAccountOrThrow(): Account {
        return currentAccount() ?: throw TrelloException("Haven't logged in, please login first")
    }
}