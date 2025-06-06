package org.kevin.trello.account.mapper.typeHandler

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello.account.model.UserStatus
import org.kevin.trello.framework.PgEnumTypeHandler

@MappedTypes(UserStatus::class)
@MappedJdbcTypes(JdbcType.OTHER)
class UserStatusTypeHandler : PgEnumTypeHandler<UserStatus>(UserStatus::class.java)