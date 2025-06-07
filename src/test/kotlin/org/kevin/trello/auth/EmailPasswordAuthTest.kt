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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.text.trimIndent

val password = "Password123!"

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class EmailPasswordAuthTest @Autowired constructor(
    val mockMvc: MockMvc,
    val accountMapper: AccountMapper,
    val authProperties: AuthProperties,
) {

    @Autowired
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @Test
    @DisplayName("Login with email and password")
    fun `email and password login`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"

        AccountInsertQuery(
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
        ).let {
            val count = accountMapper.insertAccount(it)
            assertEquals(count, 1)
        }

        // start test
        val loginRequest = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
            .andExpect( cookie().exists(authProperties.accessCookieName))
            .andExpect(cookie().exists(authProperties.refreshCookieName))
            .andDo(
                document(
                    "email-password-auth",
                    requestFields(
                        fieldWithPath("email").description("Email address of the user"),
                        fieldWithPath("password").description("Password of the user")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                        fieldWithPath("data.account").description("brief account information"),
                        fieldWithPath("data.account.uid").description("Account unique identifier"),
                        fieldWithPath("data.account.email").description("Account email address"),
                        fieldWithPath("data.account.isEmailVerified").description("Whether the email is verified"),
                        fieldWithPath("data.account.nickname").description("Account nickname"),
                        fieldWithPath("data.account.status").description("Account status"),
                        fieldWithPath("data.account.role").description("Account role")
                    ),
                    responseCookies(
                        cookieWithName(authProperties.accessCookieName).description("Access token cookie"),
                        cookieWithName(authProperties.refreshCookieName).description("Refresh token cookie")
                    )
                )
            )
    }

    @Test
    @DisplayName("Login with email and password - email not exist")
    fun `email not exist`() {
        val loginRequest = """
            {
                "email": "not-exist-email@example.com",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ResponseCode.BAD_CREDENTIALS.code))
            .andExpect(jsonPath("$.message").isNotEmpty)
            .andDo(
                document(
                    "email-password-auth-email-not-exist",
                    requestFields(
                        fieldWithPath("email").description("Email address of the user"),
                        fieldWithPath("password").description("Password of the user")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )
    }

    @Test
    @DisplayName("Login with email and password - password incorrect")
    fun `password incorrect`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"

        AccountInsertQuery(
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
        ).let {
            val count = accountMapper.insertAccount(it)
            assertEquals(count, 1)
        }

        // start test
        val loginRequest = """
            {
                "email": "not-exist-email@example.com",
                "password": "wrong-password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ResponseCode.BAD_CREDENTIALS.code))
            .andExpect(jsonPath("$.message").isNotEmpty)
            .andDo(
                document(
                    "email-password-auth-password-incorrect",
                    requestFields(
                        fieldWithPath("email").description("Email address of the user"),
                        fieldWithPath("password").description("Password of the user")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )
    }
}