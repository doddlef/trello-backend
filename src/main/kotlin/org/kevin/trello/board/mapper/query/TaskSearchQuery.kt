package org.kevin.trello.board.mapper.query

data class TaskSearchQuery(
    /**
     * Unique identifier for the task list where the task belong.
     */
    val listId: String? = null,

    /**
     * Unique identifiers for the task lists where the tasks belong.
     * If provided and listId not provided, it will search for tasks in all specified lists.
     */
    val listIds: Collection<String>? = null,

    /**
     * null: all tasks, true: finished tasks, false: unfinished tasks
     */
    val isFinished: Boolean? = null,

    /**
     * Title prefix of the task, can be used for fuzzy search.
     */
    val startWith: String? = null,

    /**
     * creator uid of the task, can be used to filter tasks created by a specific user.
     */
    val creator: String? = null,
)
