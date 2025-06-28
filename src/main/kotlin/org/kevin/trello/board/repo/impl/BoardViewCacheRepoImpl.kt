package org.kevin.trello.board.repo.impl

import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.model.BoardView
import org.kevin.trello.board.repo.BoardViewCacheRepo
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class BoardViewCacheRepoImpl(
    private val boardViewMapper: BoardViewMapper,
): BoardViewCacheRepo {
    @Cacheable("boardViews", key = "#accountUid+'-'+#boardId")
    override fun findByAccountAndBoard(
        accountUid: String,
        boardId: String
    ): BoardView? {
        return boardViewMapper.findByAccountAndBoard(accountUid, boardId)
    }

    @CacheEvict("boardViews", key = "#accountUid+'-'+#boardId")
    override fun evict(accountUid: String, boardId: String) {
    }
}