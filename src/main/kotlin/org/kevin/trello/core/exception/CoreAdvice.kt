package org.kevin.trello.core.exception

import org.kevin.trello.core.response.ApiResponse
import org.kevin.trello.core.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["org.kevin.trello"])
class CoreAdvice {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ApiResponse> {
        log.error("Unexpected error occurred: {}", e.message, e)
        val response = ApiResponse.error()
            .message("Unexpected error occurred")
            .build()
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    fun handleBusinessException(e: TrelloException): ResponseEntity<ApiResponse> {
        log.info("business error occurred: {}", e.message, e)
        val response = ApiResponse.Builder(ResponseCode.BUSINESS_ERROR)
            .message(e.message ?: "Business error occurred")
            .build()
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}