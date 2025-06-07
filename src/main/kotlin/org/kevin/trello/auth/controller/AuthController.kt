package org.kevin.trello.auth.controller

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.controller.request.EmailLoginRequest
import org.kevin.trello.auth.repo.impl.CookieService
import org.kevin.trello.auth.service.AuthService
import org.kevin.trello.auth.service.RefreshService
import org.kevin.trello.auth.service.vo.EmailPasswordAuthVO
import org.kevin.trello.core.response.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieService: CookieService,
    private val refreshService: RefreshService,
) {
    @PostMapping
    fun emailPasswordLogin(@RequestBody request: EmailLoginRequest): ResponseEntity<ApiResponse> {
        val (email, password) = request
        val authentication = authService.emailPasswordAuthenticate(EmailPasswordAuthVO(email, password))

        SecurityContextHolder.getContext().authentication = authentication

        return generateAuthedResponse(authentication.principal as Account)
    }

    private fun generateAuthedResponse(account: Account): ResponseEntity<ApiResponse> {
        val accessCookie = cookieService.generateAccessCookie(account)
        val refreshCookie = refreshService.createToken(account).let {
            cookieService.generateRefreshCookie(it.content)
        }

        val response = ApiResponse.success()
            .message("authenticate success")
            .add("account" to account.toBrief())
            .build()

        return ResponseEntity.ok()
            .headers {
                it.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                it.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            }
            .body(response)
    }
}