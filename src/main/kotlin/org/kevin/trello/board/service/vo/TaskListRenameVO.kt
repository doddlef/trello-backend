package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class TaskListRenameVO(
    val listId: String,
    val newName: String,
    val account: Account,
)
