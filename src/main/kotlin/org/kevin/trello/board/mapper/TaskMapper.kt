package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.TaskInsertQuery
import org.kevin.trello.board.mapper.query.TaskSearchQuery
import org.kevin.trello.board.model.Task

@Mapper
interface TaskMapper {
    /**
     * Find a not achieved task by its unique identifier.
     *
     * @param taskId The unique identifier of the task.
     * @return The task with the specified ID, or null if not found.
     */
    fun findByTaskId(taskId: String): Task?

    /**
     * Find a not achieved task by its unique identifier, order by position ASC.
     *
     * @param taskId The unique identifier of the task.
     * @return The task with the specified ID, or null if not found.
     */
    fun search(query: TaskSearchQuery): List<Task>

    fun insert(query: TaskInsertQuery): Int
}