package org.kevin.trello.auth.service.impl

import org.apache.commons.validator.routines.EmailValidator
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.account.mapper.query.AccountUpdateQuery
import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.AuthProperties
import org.kevin.trello.auth.mapper.EmailActiveTokenMapper
import org.kevin.trello.auth.mapper.query.EmailActiveTokenInsertQuery
import org.kevin.trello.auth.service.RegisterService
import org.kevin.trello.auth.service.vo.EmailRegisterVO
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.kevin.trello.auth.MAX_NICKNAME_LENGTH
import org.kevin.trello.auth.model.EmailActiveToken
import org.kevin.trello.core.service.impl.EmailService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RegisterServiceImpl(
    private val accountMapper: AccountMapper,
    private val emailActiveTokenMapper: EmailActiveTokenMapper,
    private val passwordEncoder: PasswordEncoder,
    private val authProperties: AuthProperties,
    private val emailService: EmailService,
): RegisterService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Validates the registration data provided in the EmailRegisterVO.
     * This method checks if the email format is valid and if the password meets the required criteria.
     * It should throw an exception if validation fails.
     * at least 8 characters long, contains at least one letter and one number.
     *
     * @param vo The EmailRegisterVO containing the registration details.
     */
    private fun isPasswordValid(password: String): Boolean {
        val regex = """^(?=.*[A-Za-z])(?=.*\d).{8,}$""".toRegex()
        return regex.matches(password)
    }

    private fun validateRegistrationData(vo: EmailRegisterVO) {
        // Validate the registration data, such as checking if the email is already registered
        // and if the password meets the required criteria.
        // This method should throw an exception if validation fails.
        val (email, password, nickname) = vo
        email.takeIf {
            EmailValidator.getInstance().isValid(email)
        } ?: throw BadArgumentException("Invalid email format: $email")
        password.takeIf {
            isPasswordValid(password)
        } ?: throw BadArgumentException("Password must be at least 8 characters long and contain at least one letter and one number.")
        nickname.takeIf {
            it.isNotBlank() && it.length <= MAX_NICKNAME_LENGTH
        } ?: throw BadArgumentException("Nickname must be non-empty and up to $MAX_NICKNAME_LENGTH characters long.")

        // check if the email is already registered
        accountMapper.findByEmail(email)?.let {
            throw BadArgumentException("Email $email is already registered, account uid: ${it.uid}")
        }
    }

    private fun insertAccount(vo: EmailRegisterVO): String {
        val query = AccountInsertQuery(
            email = vo.email,
            isEmailVerified = false,
            nickname = vo.nickname,
            password = passwordEncoder.encode(vo.password),
        )
        val count = accountMapper.insertAccount(query)
        if (count != 1) throw TrelloException("Failed to insert account for email: query: ${query}")
        return query.uid
    }

    private fun createEmailActiveToken(uid: String): String {
        val query = EmailActiveTokenInsertQuery(
            uid = uid,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusHours(authProperties.emailActiveTokenLifeHours)
        )
        val count = emailActiveTokenMapper.insertToken(query)
        if (count != 1) throw TrelloException("Failed to insert email active token for uid: $uid")

        return query.token
    }

    private fun sendEmailWithToken(email: String, token: String) {
        emailService.sendEmail(
            to = email,
            subject = "Activate your Trello account",
            body = "Please click the link to activate your account: xxxx/api/auth/verify?token=$token"
        )
    }

    @Transactional
    override fun emailRegister(vo: EmailRegisterVO): ApiResponse {
        validateRegistrationData(vo)
        val uid = insertAccount(vo)
        logger.debug("Successfully registered user with email: ${vo.email}, uid: $uid")

        val token = createEmailActiveToken(uid)
        logger.debug("Successfully generate token for email active, uid: $uid, token: $token")

        sendEmailWithToken(vo.email, token)

        return ApiResponse.success()
            .message("Registration successful, please check your email to activate your account.")
            .add("accountUid" to uid)
            .build()
    }

    private fun validateEmailToken(token: String): EmailActiveToken {
        val tokenEntity = emailActiveTokenMapper.findByToken(token)
        if (tokenEntity == null || tokenEntity.expiresAt.isBefore(LocalDateTime.now()))
            throw BadArgumentException("Invalid or expired token: $token")
        return tokenEntity
    }

    private fun activeAccount(uid: String) {
        val count = AccountUpdateQuery(isEmailVerified = true).let {
            val count = accountMapper.updateByUid(uid, it)
            count
        }

        if (count != 1) throw TrelloException("Failed to activate account for uid: $uid")
    }

    @Transactional
    override fun verificationEmail(token: String): Account {
        val (_, _, uid, _, _) = validateEmailToken(token)
        logger.debug("Email verification successful for token: $token, uid: $uid")
        activeAccount(uid)
        emailActiveTokenMapper.deleteByToken(token)
        return accountMapper.findByUID(uid)
            ?: throw TrelloException("Failed to find active account for uid: $uid")
    }

    private fun createOrReuse(uid: String): String {
        val tokenEntity = emailActiveTokenMapper.findByUid(uid)
        if (tokenEntity != null) return tokenEntity.token

        return createEmailActiveToken(uid)
    }

    @Transactional
    override fun resendVerificationEmail(email: String): ApiResponse {
        val account = accountMapper.findByEmail(email)
            ?: throw BadArgumentException("Email $email is not existed")
        if (account.isEmailVerified) throw BadArgumentException("Email $email is not existed")

        val token = createOrReuse(account.uid)
        sendEmailWithToken(email, token)

        return ApiResponse.success()
            .message("Email verification link has been resent, please check your email.")
            .add("accountUid" to account.uid)
            .build()
    }

    @Transactional
    override fun cleanUpExpiredTokens() {
        logger.info("Cleaning up expired email active tokens")
        val deletedCount = emailActiveTokenMapper.deleteAllExpiredTokens()
        logger.info("Deleted $deletedCount expired email active tokens")
    }
}