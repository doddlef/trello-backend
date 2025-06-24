package org.kevin.trello.auth.exception

import org.kevin.trello.core.exception.TrelloException

class EmailNotVerifiedException(val email: String): TrelloException("Email not verified") {
    companion object {
        private const val serialVersionUID = 1L
    }
}