package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class TaskListMoveVO(
    val listId: String,
    val afterListId: String?,
    val boardId: String,
    val account: Account,
)