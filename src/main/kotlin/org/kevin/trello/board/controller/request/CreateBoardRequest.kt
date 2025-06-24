package org.kevin.trello.board.controller.request

data class CreateBoardRequest(
    val name: String,
    val visibility: String = "PRIVATE", // Default visibility is PRIVATE
)
