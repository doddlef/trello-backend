package org.kevin.trello.core.response

enum class ResponseCode(
    val code: Int,
) {
    /**
     * Success response code.
     */
    SUCCESS(0),

    /**
     * Error response code.
     */
    ERROR(1),

    /**
     * Business error response code.
     */
    BUSINESS_ERROR(1001),

    /**
     * Access denied response code.
     */
    ACCESS_DENIED(2),

    /**
     * Access token expired response code.
     */
    TOKEN_EXPIRED(2001),

    /**
     * Bad credentials response code.
     */
    BAD_CREDENTIALS(2002),
}