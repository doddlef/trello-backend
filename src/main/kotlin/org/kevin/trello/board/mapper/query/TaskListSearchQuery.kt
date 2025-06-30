package org.kevin.trello.board.mapper.query

data class TaskListSearchQuery(
    /**
     * The unique identifier of the board to which the task list belongs.
     */
    val boardId: String? = null,

    /**
     * The unique identifier of the creator of the task list.
     */
    val createdBy: String? = null,

    /**
     * The prefix name of the task list to search for.
     */
    val startWith: String? = null,

    /**
     * The unique identifier of the task list to search for.
     * POSITION (DEFAULT): sort by position (ASC)
     * NAME: sort by name (ASC)
     * NAME_DESC: sort by name (DESC)
     */
    val orderBy: String? = null,
)
