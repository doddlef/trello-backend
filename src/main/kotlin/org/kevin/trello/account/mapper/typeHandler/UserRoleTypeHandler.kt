package org.kevin.trello.account.mapper.typeHandler

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello.account.model.UserRole
import org.kevin.trello.framework.PgEnumTypeHandler

@MappedTypes(UserRole::class)
@MappedJdbcTypes(JdbcType.OTHER)
class UserRoleTypeHandler : PgEnumTypeHandler<UserRole>(UserRole::class.java)