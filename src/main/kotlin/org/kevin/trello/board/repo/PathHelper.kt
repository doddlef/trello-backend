package org.kevin.trello.board.repo

import org.kevin.trello.board.model.BoardView
import org.kevin.trello.board.model.Task
import org.kevin.trello.board.model.TaskList

data class PathResult(
    val boardView: BoardView? = null,
    val taskList: TaskList? = null,
    val task: Task? = null,
)

interface PathHelper {
    fun pathOfBoard(boardId: String, uid: String): PathResult
    fun pathOfList(listId: String, uid: String): PathResult
    fun pathOfTask(taskId: String, uid: String): PathResult
}