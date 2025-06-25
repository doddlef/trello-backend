package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.TaskListInsertQuery
import org.kevin.trello.board.mapper.query.TaskListUpdateQuery
import org.kevin.trello.board.model.TaskList

@Mapper
interface TaskListMapper {
    fun insert(query: TaskListInsertQuery): Int
    fun updateById(query: TaskListUpdateQuery): Int
    fun findByBoard(boardId: String): List<TaskList>
    fun findByListId(listId: String): TaskList?
}