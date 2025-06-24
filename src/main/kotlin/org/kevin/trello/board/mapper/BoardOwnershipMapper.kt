package org.kevin.trello.board.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.board.mapper.query.BoardOwnerShipInsertQuery

@Mapper
interface BoardOwnershipMapper {
    fun insertBoardOwnership(query: BoardOwnerShipInsertQuery): Int
}