package org.kevin.trello.board.service.vo

import org.kevin.trello.account.model.Account

data class TaskMoveVO(
    /**
     * The ID of the task to be moved.
     */
    val taskId: String,

    /**
     * ID of the destination list where the task will be moved.
     */
    val listId: String,

    /**
     * The ID of the task after which the moved task will be placed.
     * If this is null, the task will be placed at the head of the list.
     */
    val afterId: String?,

    /**
     * the user account performing the move operation.
     */
    val account: Account,
)
