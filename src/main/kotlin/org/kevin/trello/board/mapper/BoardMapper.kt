package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.BoardInsertQuery
import org.kevin.trello.board.model.Board

@Mapper
interface BoardMapper {
    fun findBoardById(boardId: String): Board?
    fun insertBoard(query: BoardInsertQuery): Int
}