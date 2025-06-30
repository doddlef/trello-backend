package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.TaskInsertQuery
import org.kevin.trello.board.model.Task

@Mapper
interface TaskMapper {
    fun findByTaskId(taskId: String): Task?
    fun insert(query: TaskInsertQuery): Int
}