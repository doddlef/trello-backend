package org.kevin.trello.board.service.impl

import org.kevin.trello.account.model.Account
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.TaskListMapper
import org.kevin.trello.board.mapper.TaskMapper
import org.kevin.trello.board.mapper.query.TaskListSearchQuery
import org.kevin.trello.board.mapper.query.TaskSearchQuery
import org.kevin.trello.board.model.BoardContent
import org.kevin.trello.board.model.TaskContent
import org.kevin.trello.board.model.TaskListContent
import org.kevin.trello.board.service.ReadService
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.stereotype.Service

@Service
class ReadServiceImpl(
    private val boardViewMapper: BoardViewMapper,
    private val taskListMapper: TaskListMapper,
    private val taskMapper: TaskMapper,
) : ReadService {
    override fun readBoardContent(
        boardId: String,
        reader: Account
    ): ApiResponse {
        val board = boardViewMapper.findByAccountAndBoard(reader.uid, boardId)
        if (board == null) {
            throw BadArgumentException("Board with ID $boardId not exist, or you do not have permission to read it.")
        }

        val taskLists = taskListMapper.searchByQuery(
            TaskListSearchQuery(
                boardId = boardId,
            )
        )

        val taskMap = taskLists
            .map { it.listId }
            .toSet()
            .let { taskMapper.search(TaskSearchQuery(listIds = it)) }
            .map { TaskContent(it) }
            .groupBy { it.listId }
            .mapValues { it.value.sortedBy { it.position } }

        val content = taskLists
            .map {
                TaskListContent(it, taskMap.getOrElse(it.listId, { emptyList() }))
            }
            .sortedBy { it.position }
            .let { BoardContent(board, it) }

        return ApiResponse
            .success()
            .add("content" to content)
            .build()
    }
}