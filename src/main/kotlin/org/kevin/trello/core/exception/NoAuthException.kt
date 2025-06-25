package org.kevin.trello.core.exception

class NoAuthException: TrelloException {
    constructor() : super("have no authority to access this resource")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}