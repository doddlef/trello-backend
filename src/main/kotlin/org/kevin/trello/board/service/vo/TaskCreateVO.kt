package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class TaskCreateVO (
    /**
     * The title of the task.
     */
    val title: String,

    /**
     * creator of the task.
     */
    val account: Account,

    /**
     * The ID of the list where the task will be created.
     */
    val listId: String,

    /**
     * the parent task ID if this is a sub-task.
     */
    val parentId: String?,
)