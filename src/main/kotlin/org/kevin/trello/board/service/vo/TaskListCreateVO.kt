package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class TaskListCreateVO(
    val boardId: String,
    val name: String,
    val account: Account
)
