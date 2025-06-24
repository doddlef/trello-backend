package org.kevin.trello.board.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.board.controller.request.CreateBoardRequest
import org.kevin.trello.board.model.BoardVisibility
import org.kevin.trello.board.service.BoardService
import org.kevin.trello.board.service.vo.BoardCreateVO
import org.kevin.trello.board.service.vo.BoardSearchVO
import org.kevin.trello.core.exception.TrelloException
import org.kevin.trello.core.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/board")
class BoardController(
    private val boardService: BoardService,
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
}