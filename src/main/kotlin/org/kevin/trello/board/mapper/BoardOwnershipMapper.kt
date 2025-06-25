package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.BoardOwnerShipInsertQuery
import org.kevin.trello.board.mapper.query.BoardOwnershipUpdateQuery

@Mapper
interface BoardOwnershipMapper {
    fun insertBoardOwnership(query: BoardOwnerShipInsertQuery): Int
    fun updateBoardOwnership(query: BoardOwnershipUpdateQuery): Int
}