package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.model.BoardVisibility

data class BoardCreateVO(
    val name: String,
    val account: Account,
    val visibility: BoardVisibility,
    val deletedFlag: Boolean = false,
)
