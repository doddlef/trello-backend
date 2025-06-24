package org.kevin.trello.board.service

import org.kevin.trello.board.service.vo.BoardCreateVO
import org.kevin.trello.core.response.ApiResponse

interface BoardService {
    fun createNewBoard(vo: BoardCreateVO): ApiResponse
}