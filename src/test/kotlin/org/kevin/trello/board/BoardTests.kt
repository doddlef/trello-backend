package org.kevin.trello.board

import jakarta.servlet.http.Cookie
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.auth.AuthProperties
import org.kevin.trello.core.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.cookies.CookieDocumentation.requestCookies
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.cookies.CookieDocumentation.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class BoardTests @Autowired constructor(
    val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountMapper: AccountMapper,
) {
    private val email = "${RandomString(8).nextString()}@example.com"
    private val nickname = "${RandomString(4).nextString()}-user"
    private val password = "Password123!"

    private var accessCookie: Cookie? = null
    private var refreshCookie: Cookie? = null

    @BeforeEach
    fun setUp() {
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

        accessCookie = loginResult.response.getCookie(authProperties.accessCookieName)
        refreshCookie = loginResult.response.getCookie(authProperties.refreshCookieName)
    }

    @Test
    @DisplayName("create board")
    fun `create board`() {
        val boardName = "test board"
        val requestBody = """
            {
                "name": "$boardName",
                "visibility": "PRIVATE"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/board")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.board.name").value(boardName))
            .andDo(
                document(
                    "create-new-board",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("access token cookie")
                    ),
                    requestFields(
                        fieldWithPath("name").description("Name of the board"),
                        fieldWithPath("visibility").optional().description("Visibility of the board"),
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                        fieldWithPath("data.board.boardId").description("Board unique identifier"),
                        fieldWithPath("data.board.name").description("Name of the board"),
                        fieldWithPath("data.board.visibility").description("Visibility of the board"),
                        fieldWithPath("data.board.uid").description("user ID"),
                        fieldWithPath("data.board.readOnly").description("Whether the board is read-only"),
                        fieldWithPath("data.board.addedAt").description("Board creation timestamp"),
                        fieldWithPath("data.board.lastOpen").description("Last opened timestamp"),
                        fieldWithPath("data.board.isFavorite").description("Whether the board is marked as favorite"),
                    )
                )
            )
    }
}