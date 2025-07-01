package org.kevin.trello.board.service

import org.kevin.trello.account.model.Account
import org.kevin.trello.core.response.ApiResponse

interface ReadService {
    /**
     * Reads the content of a board by its ID.
     * @param boardId The ID of the board to read.
     * @param reader The account of the user reading the board.
     */
    fun readBoardContent(boardId: String, reader: Account): ApiResponse
}