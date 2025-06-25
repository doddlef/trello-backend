package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.kevin.trello.board.mapper.query.BoardViewSearchQuery
import org.kevin.trello.board.model.BoardView

@Mapper
interface BoardViewMapper {
    fun findByAccountAndBoard(@Param("uid") uid: String, @Param("boardId") boardId: String): BoardView?
    fun searchBoardView(query: BoardViewSearchQuery): List<BoardView>
}