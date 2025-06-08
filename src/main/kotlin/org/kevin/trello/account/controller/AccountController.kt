package org.kevin.trello.account.controller

import org.kevin.trello.auth.utils.SecurityUtils
import org.kevin.trello.core.response.ApiResponse
import org.kevin.trello.core.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/me")
    fun getAccountInfo(): ResponseEntity<ApiResponse> {
        val currentAccount = SecurityUtils.currentAccount()
        if (currentAccount == null) {
            return ResponseEntity(
                ApiResponse.Builder(ResponseCode.NEED_LOGIN)
                    .message("have not logged in")
                    .build(),
                HttpStatus.UNAUTHORIZED
            )
        }

        return ResponseEntity(
            ApiResponse.success()
                .add("account" to currentAccount.toBrief())
                .build(),
            HttpStatus.OK
        )
    }
}