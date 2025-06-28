package org.kevin.trello.board.repo

import org.kevin.trello.board.model.BoardView

interface BoardViewCacheRepo {
    fun findByAccountAndBoard(accountUid: String, boardId: String): BoardView?
    fun evict(accountUid: String, boardId: String)
}