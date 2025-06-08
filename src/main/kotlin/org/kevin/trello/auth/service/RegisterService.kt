package org.kevin.trello.auth.service

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.service.vo.EmailRegisterVO
import org.kevin.trello.core.response.ApiResponse
import org.springframework.security.core.Authentication

interface RegisterService {
    /**
     * Registers a new user with the provided email and password.
     * if successful, an email verification will be sent to the user.
     * The created account should not be active until the user verifies their email address.
     *
     * @param vo The value object containing the registration details.
     */
    fun emailRegister(vo: EmailRegisterVO): ApiResponse

    /**
     * Verifies the user's email address using the provided token.
     * If the token is valid, the user's account will be activated.
     *
     * @param token The verification token sent to the user's email.
     * @return An Authentication object if the verification is successful.
     */
    fun verificationEmail(token: String): Account

    /**
     * Resends the verification email to the user.
     * This is typically used when the user did not receive the initial verification email,
     * or user login but not verified yet.
     *
     * @param email The email address of the user to whom the verification email should be resent.
     * @return An ApiResponse indicating the success or failure of the operation.
     */
    fun resendVerificationEmail(email: String): ApiResponse

    /**
     * Cleans up expired email verification tokens.
     * This method should be called periodically to remove tokens that are no longer valid.
     */
    fun cleanUpExpiredTokens()
}