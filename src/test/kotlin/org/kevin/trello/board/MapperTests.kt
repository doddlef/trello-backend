package org.kevin.trello.board

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.board.mapper.BoardMapper
import org.kevin.trello.board.mapper.BoardOwnershipMapper
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.query.BoardInsertQuery
import org.kevin.trello.board.mapper.query.BoardOwnerShipInsertQuery
import org.kevin.trello.board.mapper.query.BoardViewSearchQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class MapperTests @Autowired constructor(
    private val accountMapper: AccountMapper,
    private val boardMapper: BoardMapper,
    private val boardOwnershipMapper: BoardOwnershipMapper,
    private val boardViewMapper: BoardViewMapper,
) {
    private var uid: String = ""

    @BeforeEach
    fun setUp() {
        AccountInsertQuery(
            email = "not exist email@test.com",
            password = "NOT USED PASSWORD",
            nickname = "test",
        ).let {
            accountMapper.insertAccount(it)
            uid = it.uid
        }
    }

    @Test
    @DisplayName("insert new board and get view of it")
    fun `insert and view`() {
        val boardName = "test board"

        val boardId = BoardInsertQuery(
            name = boardName,
            ownerUid = uid,
        ).let {
            val count = boardMapper.insertBoard(it)
            assertEquals(1, count)
            it.boardId
        }

        BoardOwnerShipInsertQuery(
            boardId = boardId,
            uid = uid,
        ).let {
            val count = boardOwnershipMapper.insertBoardOwnership(it)
            assertEquals(1, count)
        }

        val boardViews = BoardViewSearchQuery(
            uid = uid
        ).let {
            val views = boardViewMapper.searchBoardView(it)
            views
        }

        assertEquals(1, boardViews.size)

        val first = boardViews.first()
        assertEquals(boardId, first.boardId)
        assertEquals(boardName, first.name)
        println(first)
    }
}