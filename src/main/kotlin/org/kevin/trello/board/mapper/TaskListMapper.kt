package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.TaskListInsertQuery
import org.kevin.trello.board.mapper.query.TaskListSearchQuery
import org.kevin.trello.board.mapper.query.TaskListUpdateQuery
import org.kevin.trello.board.model.TaskList

@Mapper
interface TaskListMapper {
    fun insert(query: TaskListInsertQuery): Int
    fun updateById(query: TaskListUpdateQuery): Int

    /**
     * Find all not achieved task lists associated with a specific board, ordered by position ASC.
     *
     * @param boardId The unique identifier of the board to which the task lists belong.
     */
    fun findByBoard(boardId: String): List<TaskList>

    /**
     * Search for not achieved task lists based on the provided query parameters
     *
     * @param query The search criteria for task lists.
     * @return A list of task lists that match the search criteria.
     */
    fun searchByQuery(query: TaskListSearchQuery): List<TaskList>

    /**
     * Find a not achieved task list by its unique identifier
     */
    fun findByListId(listId: String): TaskList?
}