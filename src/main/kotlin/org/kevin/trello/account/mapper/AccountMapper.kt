package org.kevin.trello.account.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.account.mapper.query.AccountUpdateQuery
import org.kevin.trello.account.model.Account

@Mapper
interface AccountMapper {
    fun findByEmail(email: String): Account?
    fun findByUID(uid: String): Account?
    fun insertAccount(query: AccountInsertQuery): Int
    fun updateByUid(@Param("uid") uid: String, @Param("query") query: AccountUpdateQuery): Int

//    TEST functions, do not used in production
    fun deleteAll()
}