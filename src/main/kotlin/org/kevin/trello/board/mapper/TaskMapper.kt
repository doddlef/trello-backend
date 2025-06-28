package org.kevin.trello.board.mapper

import org.kevin.trello.board.model.Task

interface TaskMapper {
    fun findByTaskId(taskId: String): Task?
}