package org.kevin.trello.auth.controller.request

data class EmailLoginRequest(
    val email: String,
    val password: String
)
