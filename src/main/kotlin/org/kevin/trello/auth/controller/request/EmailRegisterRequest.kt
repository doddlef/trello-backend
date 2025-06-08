package org.kevin.trello.auth.controller.request

data class EmailRegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
)
