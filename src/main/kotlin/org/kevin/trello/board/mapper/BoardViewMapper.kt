package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.BoardViewSearchQuery
import org.kevin.trello.board.model.BoardView

@Mapper
interface BoardViewMapper {
    fun searchBoardView(query: BoardViewSearchQuery): List<BoardView>
}