package org.kevin.trello.board.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.board.controller.request.CreateBoardRequest
import org.kevin.trello.board.model.BoardVisibility
import org.kevin.trello.board.service.BoardService
import org.kevin.trello.board.service.ReadService
import org.kevin.trello.board.service.vo.BoardCreateVO
import org.kevin.trello.board.service.vo.BoardSearchVO
import org.kevin.trello.board.service.vo.BoardSelectVO
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/board")
class BoardController(
    private val boardService: BoardService,
    private val readService: ReadService,
) {
    @PostMapping
    fun createNewBoard(@RequestBody request: CreateBoardRequest): ApiResponse {
        val (name, visibilityStr) = request
        val account = SecurityUtils.currentAccount()
            ?: throw TrelloException("User must be logged in to create a board")

        return BoardCreateVO(
            name = name.trim(),
            account = account,
            visibility = BoardVisibility.valueOf(visibilityStr),
            deletedFlag = false
        ).let {
            val response = boardService.createNewBoard(it)
            response
        }
    }

    @GetMapping
    fun boardList(
        @RequestParam("startWith", required = false) startWith: String?,
        @RequestParam("orderBy", required = false) orderBy: String?,
        @RequestParam("offset", defaultValue = "0") offset: Int,
        @RequestParam("limit", defaultValue = "16") limit: Int,
    ): ApiResponse {
        val account = SecurityUtils.currentAccount()
            ?: throw TrelloException("You must be logged in to view boards")

        BoardSearchVO(
            account = account,
            startWith = startWith,
            orderBy = orderBy,
            offset = offset,
            limit = limit
        ).let {
            val response = boardService.boardList(it)
            return response
        }
    }

    @PostMapping("/{boardId}/like")
    fun likeBoard(@PathVariable("boardId") boardId: String): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()

        BoardSelectVO(
            account = account,
            boardId = boardId
        ).let {
            return boardService.likeBoard(it)
        }
    }

    @PostMapping("/{boardId}/dislike")
    fun dislikeBoard(@PathVariable("boardId") boardId: String): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()

        BoardSelectVO(
            account = account,
            boardId = boardId
        ).let {
            return boardService.dislikeBoard(it)
        }
    }

    @GetMapping("/{boardId}")
    fun content(@PathVariable("boardId") boardId: String): ApiResponse {
        val account = SecurityUtils.currentAccountOrThrow()
        return readService.readBoardContent(boardId, account)
    }
}