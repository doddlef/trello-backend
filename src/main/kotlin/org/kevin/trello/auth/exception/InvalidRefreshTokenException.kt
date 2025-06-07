package org.kevin.trello.auth.exception

import org.kevin.trello.core.exception.TrelloException

class InvalidRefreshTokenException(
    message: String = "Invalid refresh token"
): TrelloException(message)