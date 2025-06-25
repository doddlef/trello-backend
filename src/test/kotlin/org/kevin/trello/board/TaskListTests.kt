package org.kevin.trello.board

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.Cookie
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello.account.mapper.AccountMapper
import org.kevin.trello.account.mapper.query.AccountInsertQuery
import org.kevin.trello.auth.AuthProperties
import org.kevin.trello.board.mapper.TaskListMapper
import org.kevin.trello.core.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class TaskListTests @Autowired constructor(
    val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountMapper: AccountMapper,
    val taskListMapper: TaskListMapper,
) {
    private val email = "${RandomString(8).nextString()}@example.com"
    private val nickname = "${RandomString(4).nextString()}-user"
    private val password = "Password123!"

    private lateinit var accountUid: String;
    private lateinit var boardId: String;
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
            accountUid = it.uid
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
            .andReturn()
            .response
            .contentAsString
            .let {
                val str = jacksonObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("board")
                    .path("boardId")
                    .asText()
                assertNotNull(str, "Board ID should not be null after creation")
                boardId = str
            }
    }

    @Test
    fun `create task list`() {
        val requestBody = """
            {
                "name": "1",
                "boardId": "$boardId"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/tasklist")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andDo(
                document(
                    "create-task-list",
                    requestFields(
                        fieldWithPath("name").description("The name of the task list"),
                        fieldWithPath("boardId").description("The ID of the board to which the task list belongs")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code indicating success or failure"),
                        fieldWithPath("message").optional().description("A message describing the result of the operation"),
                        fieldWithPath("data.listId").description("The ID of the newly created task list"),
                        fieldWithPath("data.position").description("The position of the task list in the board"),
                    ),
                )
            )

        val secondTask = """
            {
                "name": "2",
                "boardId": "$boardId"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/tasklist")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondTask)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))

        taskListMapper.findByBoard(boardId).let {
            assertEquals(2, it.size, "There should be 2 task lists in the board")
            assertEquals("1", it[0].name, "First task list should have name '1'")
            assertEquals("2", it[1].name, "Second task list should have name '2'")
            assert(it[0].position < it[1].position, { "First task list should have a lower position than the second" })
        }
    }
}