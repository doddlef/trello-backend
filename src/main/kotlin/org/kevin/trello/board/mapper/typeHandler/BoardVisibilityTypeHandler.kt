package org.kevin.trello.board.mapper.typeHandler

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello.board.model.BoardVisibility
import org.kevin.trello.framework.PgEnumTypeHandler

@MappedTypes(BoardVisibility::class)
@MappedJdbcTypes(JdbcType.OTHER)
class BoardVisibilityTypeHandler: PgEnumTypeHandler<BoardVisibility>(BoardVisibility::class.java)