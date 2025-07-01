package org.kevin.trello.board.model

import java.time.LocalDate

class BoardContent (
    val id: String,
    val uid: String,
    val name: String,
    val visibility: BoardVisibility,
    val readOnly: Boolean,
    val isFavorite: Boolean,
    val lists: Collection<TaskListContent>,
) {
    constructor(boardView: BoardView, lists: Collection<TaskListContent>) : this(
        id = boardView.boardId,
        name = boardView.name,
        visibility = boardView.visibility,
        uid = boardView.uid,
        readOnly = boardView.readOnly,
        isFavorite = boardView.isFavorite,
        lists = lists
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class TaskListContent(
    val id: String,
    val boardId: String,
    val name: String,
    val position: Int,
    val tasks: Collection<TaskContent>,
) {
    constructor(taskList: TaskList, tasks: Collection<TaskContent>) : this(
        id = taskList.listId,
        boardId = taskList.boardId,
        name = taskList.name,
        position = taskList.position,
        tasks = tasks,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskListContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class TaskContent(
    val id: String,
    val listId: String,
    val finished: Boolean,
    val position: Double,
    val title: String,
    val description: String?,
    val date: LocalDate?,
) {
    constructor(task: Task) : this(
        id = task.taskId,
        listId = task.listId,
        finished = task.finished,
        position = task.position,
        title = task.title,
        description = task.description,
        date = task.date
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}