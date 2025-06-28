package org.kevin.trello.board.repo.impl

import org.kevin.trello.board.mapper.TaskListMapper
import org.kevin.trello.board.repo.BoardViewCacheRepo
import org.kevin.trello.board.repo.PathHelper
import org.kevin.trello.board.repo.PathResult
import org.springframework.stereotype.Repository

@Repository
class PathHelperImpl(
    private val boardViewCacheRepo: BoardViewCacheRepo,
    private val taskListMapper: TaskListMapper,
): PathHelper {
    override fun pathOfBoard(boardId: String, uid: String): PathResult {
        return PathResult(
            boardView = boardViewCacheRepo.findByAccountAndBoard(uid, boardId),
        )
    }

    override fun pathOfList(listId: String, uid: String): PathResult {
        val taskList = taskListMapper.findByListId(listId)
        val boardView = taskList?.let {
            boardViewCacheRepo.findByAccountAndBoard(uid, it.boardId)
        }
        return PathResult(
            boardView = boardView,
            taskList = taskList,
        )
    }
}