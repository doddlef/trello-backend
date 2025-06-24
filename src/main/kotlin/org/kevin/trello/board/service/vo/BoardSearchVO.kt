package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class BoardSearchVO(
    val account: Account,
    val startWith: String? = null,
    val orderBy: String? = null,
    val offset: Int = 0,
    val limit: Int = 16,
)