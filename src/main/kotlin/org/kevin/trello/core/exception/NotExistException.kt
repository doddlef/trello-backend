package org.kevin.trello.core.exception

class NotExistException: TrelloException {
    constructor() : super("The requested resource does not exist.")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}