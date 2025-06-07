package org.kevin.trello.auth.exception

import org.kevin.trello.auth.model.RefreshToken
import org.kevin.trello.core.exception.TrelloException

class RefreshTokenExpiredException: TrelloException("token has expired") {
    companion object {
        private const val serialVersionUID = 1L
    }
}