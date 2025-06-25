package org.kevin.trello.board.service

import org.kevin.trello.board.service.vo.BoardCreateVO
import org.kevin.trello.board.service.vo.BoardSearchVO
import org.kevin.trello.board.service.vo.BoardSelectVO
import org.kevin.trello.core.response.ApiResponse

interface BoardService {
    fun createNewBoard(vo: BoardCreateVO): ApiResponse
    fun boardList(vo: BoardSearchVO): ApiResponse
    fun likeBoard(vo: BoardSelectVO): ApiResponse
    fun dislikeBoard(vo: BoardSelectVO): ApiResponse
}