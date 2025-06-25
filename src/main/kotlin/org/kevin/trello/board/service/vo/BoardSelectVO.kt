package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class BoardSelectVO(
    val boardId: String,
    val account: Account,
)
