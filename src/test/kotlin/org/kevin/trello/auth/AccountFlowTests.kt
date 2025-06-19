package org.kevin.trello.auth

import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.core.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.cookies.CookieDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class AccountFlowTests @Autowired constructor(
    val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val accountMapper: AccountMapper,
    val passwordEncoder: PasswordEncoder,
) {

    @Test
    @DisplayName("login, refresh, logout")
    fun `login, refresh, logout`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        AccountInsertQuery(
            email = email,
            nickname = nickname,
            password = passwordEncoder.encode(password),
            isEmailVerified = true
        ).let {
            val count = accountMapper.insertAccount(it)
            assertEquals(1, count)
        }

        // login
        val loginRequest = """
            {
                "email": "$email",
                "password": "${org.kevin.trello.auth.password}"
            }
        """.trimIndent()

        val loginResult = mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
            .andExpect( cookie().exists(authProperties.accessCookieName))
            .andExpect(cookie().exists(authProperties.refreshCookieName))
            .andReturn()

        var accessCookie = loginResult.response.getCookie(authProperties.accessCookieName)
        val refreshCookie = loginResult.response.getCookie(authProperties.refreshCookieName)

        // check account
        mockMvc.perform(
            get("/api/account/me")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
            .andDo(
                document(
                    "get-account-info",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("access token cookie")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("data.account").description("brief account information"),
                        fieldWithPath("data.account.uid").description("Account unique identifier"),
                        fieldWithPath("data.account.email").description("Account email address"),
                        fieldWithPath("data.account.isEmailVerified").description("Whether the email is verified"),
                        fieldWithPath("data.account.nickname").description("Account nickname"),
                        fieldWithPath("data.account.status").description("Account status"),
                        fieldWithPath("data.account.role").description("Account role"),
                        fieldWithPath("data.account.avatarUrl").description("Account avatar URL"),
                    )
                )
            )

        // refresh
        val refreshResult = mockMvc.perform(
            post("/api/auth/refresh")
                .cookie(refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(cookie().exists(authProperties.accessCookieName))
            .andDo(
                document(
                    "refresh",
                    requestCookies(
                        cookieWithName(authProperties.refreshCookieName).description("refresh token cookie")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    ),
                    responseCookies(
                        cookieWithName(authProperties.accessCookieName).description("New access token cookie")
                    )
                )
            )
            .andReturn()

        accessCookie = refreshResult.response.getCookie(authProperties.accessCookieName)

        // logout
        mockMvc.perform(
            post("/api/auth/logout")
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(cookie().exists(authProperties.accessCookieName))
            .andExpect(cookie().exists(authProperties.refreshCookieName))
            .andDo(
                document(
                    "logout",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("Access token cookie, should be cleared"),
                        cookieWithName(authProperties.refreshCookieName).description("Refresh token cookie, should be cleared")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    ),
                    responseCookies(
                        cookieWithName(authProperties.accessCookieName).description("cleaned access token cookie"),
                        cookieWithName(authProperties.refreshCookieName).description("cleaned refresh token cookie")
                    )
                )
            )
    }

    @Test
    @DisplayName("check account info without login")
    fun `need login`() {
        mockMvc.perform(
            get("/api/account/me")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ResponseCode.NEED_LOGIN.code))
            .andDo(document(
                "check-account-info-unauthorized",
                responseFields(
                    fieldWithPath("code").description("Response code"),
                    fieldWithPath("message").description("Response message")
                ),
            ))
    }
}