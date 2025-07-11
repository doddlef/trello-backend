package org.kevin.trello.auth.service.impl

import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.AuthProperties
import org.kevin.trello.auth.exception.InvalidRefreshTokenException
import org.kevin.trello.auth.exception.RefreshTokenExpiredException
import org.kevin.trello.auth.mapper.RefreshTokenMapper
import org.kevin.trello.auth.model.RefreshToken
import org.kevin.trello.auth.service.RefreshService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class RefreshServiceImpl(
    private val refreshTokenMapper: RefreshTokenMapper,
    private val authProperties: AuthProperties,
    private val accountMapper: AccountMapper,
): RefreshService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun findByTokenContent(content: String): RefreshToken? {
        return refreshTokenMapper.findByContent(content)
    }

    @Transactional
    override fun createToken(account: Account): RefreshToken {
        val token = RefreshToken(
            content = UUID.randomUUID().toString(),
            accountUid = account.uid,
            expireAt = LocalDateTime.now().plusDays(authProperties.refreshTokenLifeDays)
        )
        val count = refreshTokenMapper.save(token)
        if (count != 1) {
            throw IllegalStateException("Failed to create refresh token for account: ${account.uid}")
        }
        return token
    }

    override fun verifyToken(
        token: RefreshToken
    ): RefreshToken {
        if (token.expireAt.isBefore(LocalDateTime.now()))
            throw RefreshTokenExpiredException()
        return token
    }

    override fun getAccountByToken(token: RefreshToken): Account {
        return accountMapper.findByUID(token.accountUid)
            ?: throw InvalidRefreshTokenException("No account found for token: ${token.content}")
    }

    @Transactional
    override fun deleteToken(token: String) {
        refreshTokenMapper.deleteByContent(token)
    }

    @Transactional
    override fun cleanUpExpiredTokens() {
        log.info("Cleaning up expired tokens")
        refreshTokenMapper.deleteAllExpired().let {
            if (it > 0) {
                log.info("Cleaned up $it expired tokens")
            } else {
                log.info("No expired tokens found")
            }
        }
    }
}