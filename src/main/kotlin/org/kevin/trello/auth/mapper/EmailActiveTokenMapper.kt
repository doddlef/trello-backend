package org.kevin.trello.auth.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello.auth.mapper.query.EmailActiveTokenInsertQuery
import org.kevin.trello.auth.model.EmailActiveToken

@Mapper
interface EmailActiveTokenMapper {
    fun insertToken(query: EmailActiveTokenInsertQuery): Int
    fun findByToken(token: String): EmailActiveToken?
    fun findByUid(uid: String): EmailActiveToken?
    fun findByEmail(email: String): EmailActiveToken?
    fun deleteByToken(token: String): Int
    fun deleteAllExpiredTokens(): Int
}