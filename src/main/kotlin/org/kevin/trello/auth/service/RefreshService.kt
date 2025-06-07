package org.kevin.trello.auth.service

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.model.RefreshToken

interface RefreshService {
    /**
     * Find a refresh token by its content.
     *
     * @param content The content of the refresh token.
     * @return The found refresh token, or null if not found.
     */
    fun findByTokenContent(content: String): RefreshToken?

    /**
     * Create a new refresh token for the given account.
     *
     * @param account The account for which to create the refresh token.
     * @return The newly created refresh token.
     */
    fun createToken(account: Account): RefreshToken

    /**
     * Verify the given refresh token.
     *
     * @param token The refresh token to verify.
     * @param user The account associated with the token.
     * @return The verified refresh token.
     * @throws org.kevin.trello.auth.exception.RefreshTokenExpiredException if the token has expired.
     * @throws org.kevin.trello.auth.exception.InvalidRefreshTokenException if the token is invalid or does not match the user.
     */
    fun verifyToken(token: RefreshToken, user: Account): RefreshToken

    /**
     * Delete all the expired tokens from the database.
     */
    fun cleanUpExpiredTokens()
}