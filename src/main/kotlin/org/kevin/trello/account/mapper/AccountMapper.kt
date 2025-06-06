package org.kevin.trello.account.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.account.model.Account

@Mapper
interface AccountMapper {
    fun findByEmail(email: String): Account?
    fun findByUID(uid: String): Account?
    fun insertAccount(query: AccountInsertQuery): Int

//    TEST functions, do not used in production
    fun deleteAll()
}