package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account
import java.time.LocalDate

data class TaskUpdateVO(
    val taskId: String,
    val title: String? = null,
    val description: String? = null,
    val date: LocalDate? = null,

    val account: Account,
)
