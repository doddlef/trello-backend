package org.kevin.trello.auth.controller

import org.kevin.trello.account.model.Account
import org.kevin.trello.auth.controller.request.EmailActiveRequest
import org.kevin.trello.auth.controller.request.EmailLoginRequest
import org.kevin.trello.auth.controller.request.EmailRegisterRequest
import org.kevin.trello.auth.model.AccountDetailAdaptor
import org.kevin.trello.auth.repo.impl.CookieService
import org.kevin.trello.auth.service.AuthService
import org.kevin.trello.auth.service.RefreshService
import org.kevin.trello.auth.service.RegisterService
import org.kevin.trello.auth.service.vo.EmailPasswordAuthVO
import org.kevin.trello.auth.service.vo.EmailRegisterVO
import org.kevin.trello.core.response.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val registerService: RegisterService,
    private val cookieService: CookieService,
    private val refreshService: RefreshService,
) {
    @PostMapping
    fun emailPasswordLogin(@RequestBody request: EmailLoginRequest): ResponseEntity<ApiResponse> {
        val (email, password) = request
        val authentication = authService.emailPasswordAuthenticate(EmailPasswordAuthVO(email, password))

        SecurityContextHolder.getContext().authentication = authentication

        return generateAuthedResponse((authentication.principal as AccountDetailAdaptor).account)
    }

    @PostMapping("/register")
    fun emailPasswordRegister(@RequestBody request: EmailRegisterRequest): ApiResponse {
        val (email, password, nickname) = request
        return registerService.emailRegister(EmailRegisterVO(email, password, nickname))
    }

    @PutMapping("/active")
    fun activeEmail(@RequestBody request: EmailActiveRequest): ResponseEntity<ApiResponse> {
        val account = registerService.verificationEmail(request.token)
        return generateAuthedResponse(account)
    }

    @GetMapping("/resend-token")
    fun resendToken(@RequestParam("email") email: String): ApiResponse {
        return registerService.resendVerificationEmail(email)
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