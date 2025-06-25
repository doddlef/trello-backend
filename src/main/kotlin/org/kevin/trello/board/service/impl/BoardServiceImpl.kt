package org.kevin.trello.board.service.impl

import com.github.pagehelper.PageHelper
import com.github.pagehelper.PageInfo
import org.kevin.trello.account.model.Account
import org.kevin.trello.board.mapper.BoardMapper
import org.kevin.trello.board.mapper.BoardOwnershipMapper
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.query.BoardInsertQuery
import org.kevin.trello.board.mapper.query.BoardOwnerShipInsertQuery
import org.kevin.trello.board.mapper.query.BoardOwnershipUpdateQuery
import org.kevin.trello.board.mapper.query.BoardViewSearchQuery
import org.kevin.trello.board.model.BoardView
import org.kevin.trello.board.service.BoardService
import org.kevin.trello.board.service.vo.BoardCreateVO
import org.kevin.trello.board.service.vo.BoardSearchVO
import org.kevin.trello.board.service.vo.BoardSelectVO
import org.kevin.trello.core.exception.BadArgumentException
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val boardViewMapper: BoardViewMapper,
    private val boardOwnershipMapper: BoardOwnershipMapper,
): BoardService {

    private fun validateCreateRequest(vo: BoardCreateVO) {
        if (vo.name.isBlank() || vo.name.length > 255) {
            throw BadArgumentException("Board name must be between 1 and 255 characters")
        }
    }

    private fun insertBoard(vo: BoardCreateVO): String {
        BoardInsertQuery(
            name = vo.name,
            ownerUid = vo.account.uid,
            visibility = vo.visibility,
            deletedFlag = vo.deletedFlag
        ).let {
            boardMapper.insertBoard(it)
            return it.boardId
        }
    }

    private fun insertBoardOwnership(boardId: String, account: Account) {
        BoardOwnerShipInsertQuery(
            boardId = boardId,
            uid = account.uid,
            readOnly = false,
            isFavorite = false
        ).let {
            boardOwnershipMapper.insertBoardOwnership(it)
        }
    }

    @Transactional
    override fun createNewBoard(vo: BoardCreateVO): ApiResponse {
        // validate request
        validateCreateRequest(vo)
        // create board
        val boardId = insertBoard(vo)
        // create ownership
        insertBoardOwnership(boardId, vo.account)
        // construct response
        val boardView = BoardViewSearchQuery(
            uid = vo.account.uid,
            boardId = boardId
        ).let {
            val view = (boardViewMapper.searchBoardView(it)
                .firstOrNull()
                ?: throw TrelloException("Failed to create board, please try again later"))
            view
        }
        return ApiResponse
            .success()
            .message("Board created, id: $boardId")
            .add("board" to boardView)
            .build()
    }

    override fun boardList(vo: BoardSearchVO): ApiResponse {
        val (account, startWith, orderBy, offset, limit) = vo
        val query = BoardViewSearchQuery(
            uid = account.uid,
            startWith = startWith,
            orderBy = orderBy,
        )
        PageHelper.offsetPage<BoardView>(offset, limit).use {
            val list = boardViewMapper.searchBoardView(query)
            val info = PageInfo(list)
            return ApiResponse
                .success()
                .add("boards" to info.list)
                .add("hasNext" to info.isHasNextPage)
                .add("hasPrevious" to info.isHasPreviousPage)
                .add("total" to info.total)
                .add("offset" to offset)
                .add("limit" to limit)
                .build()
        }
    }

    @Transactional
    override fun likeBoard(vo: BoardSelectVO): ApiResponse {
        BoardOwnershipUpdateQuery(
            boardId = vo.boardId,
            uid = vo.account.uid,
            isFavorite = true,
        ).let {
            boardOwnershipMapper.updateBoardOwnership(it)
        }

        return ApiResponse.success().build()
    }

    @Transactional
    override fun dislikeBoard(vo: BoardSelectVO): ApiResponse {
        BoardOwnershipUpdateQuery(
            boardId = vo.boardId,
            uid = vo.account.uid,
            isFavorite = false,
        ).let {
            boardOwnershipMapper.updateBoardOwnership(it)
        }

        return ApiResponse.success().build()
    }
}