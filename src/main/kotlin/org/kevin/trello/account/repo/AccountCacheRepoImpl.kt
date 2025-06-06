package org.kevin.trello.account.repo

import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.model.Account
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class AccountCacheRepoImpl(
    private val accountMapper: AccountMapper,
): AccountCacheRepo {

    @Cacheable("accounts", key = "#uid")
    override fun find(uid: String): Account? {
        return accountMapper.findByUID(uid)
    }

    @CacheEvict("accounts", key = "#uid")
    override fun evict(uid: String) {
        // evict does not need to return anything
    }

    @CachePut("accounts", key = "#account.uid")
    override fun save(account: Account): Account {
        return account
    }
}