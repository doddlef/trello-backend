package org.kevin.trello.account

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class MapperTests {
    private val mapper: AccountMapper

    @Autowired
    constructor(mapper: AccountMapper) {
        this.mapper = mapper
    }

    @BeforeEach
    fun setUp() {
        mapper.deleteAll()
    }

    @Test
    fun selectNull() {
        mapper.findByEmail("SELECT NULL EMAIL").let {
            assertNull(it)
        }
    }

    @Test
    fun `test insert`() {
        val email = "NOT EXIST EMAIL"
        val query = AccountInsertQuery(
            email = email,
            nickname = "NOT EXIST NICKNAME",
        )

        val count = mapper.insertAccount(query)
        assertEquals(1, count)

        mapper.findByEmail(email).let {
            assertNotNull(it)
            assertEquals(email, it.email)
            assertEquals(query.uid, it.uid)
        }
    }
}