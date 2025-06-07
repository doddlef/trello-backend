package org.kevin.trello.auth.service.vo

data class EmailRegisterVO(
    val email: String,
    val password: String,
    val nickname: String,
)