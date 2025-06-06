package org.kevin.trello.account.repo

import org.kevin.trello.account.model.Account

interface AccountCacheRepo {
    fun find(uid: String): Account?
    fun evict(uid: String)
    fun save(account: Account): Account
}