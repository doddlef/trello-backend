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
import org.kevin.trello.core.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class TaskTests @Autowired constructor(
    val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountMapper: AccountMapper,
) {
    private val email = "${RandomString(8).nextString()}@example.com"
    private val nickname = "${RandomString(4).nextString()}-user"
    private val password = "Password123!"

    private lateinit var accountUid: String
    private lateinit var boardId: String
    private lateinit var listId: String
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

        val listCreateBody = """
            {
                "name": "1",
                "boardId": "$boardId"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/tasklist")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(listCreateBody)
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
                    .path("listId")
                    .asText()
                assertNotNull(str, "List ID should not be null after creation")
                listId = str
            }
    }

    @Test
    @DisplayName("create new task")
    fun `create new task`() {
        val firstRequestBody = """
            {
                "title": "Task 1",
                "listId": "$listId"
            }
        """.trimIndent()

        val parentId = mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value("Task 1"))
            .andExpect(jsonPath("$.data.task.listId").value(listId))
            .andReturn()
            .response
            .contentAsString
            .let {
                val str = jacksonObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("task")
                    .path("taskId")
                    .asText()
                str
            }

        assertNotNull(parentId, "Parent task ID should not be null after creation")

        val subtaskRequestBody = """
            {
                "title": "Subtask 1",
                "listId": "$listId",
                "parentId": "$parentId"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(subtaskRequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value("Subtask 1"))
            .andExpect(jsonPath("$.data.task.listId").value(listId))
            .andExpect(jsonPath("$.data.task.parentId").value(parentId)
        )
            .andDo(
                document(
                    "create-task",
                    requestFields(
                        fieldWithPath("listId").type(String::class.java).description("ID of the list to which the task belongs"),
                        fieldWithPath("parentId").type(String::class.java).description("The parent task belongs").optional(),
                        fieldWithPath("title").type(String::class.java).description("Title of the task"),
                    ),
                    requestFields(
                        fieldWithPath("code").description("Response code indicating success or failure"),
                        fieldWithPath("message").optional().description("A message describing the result of the operation"),
                        fieldWithPath("data.task.taskId").description("Unique identifier for the task"),
                        fieldWithPath("data.task.listId").description("Unique identifier for the list to which this task belongs"),
                        fieldWithPath("data.task.parentId").optional().description("Unique identifier for the parent task, if this task is a subtask"),
                        fieldWithPath("data.task.position").description("Position of the task in the list"),
                        fieldWithPath("data.task.finished").description("Indicates whether the task is finished"),
                        fieldWithPath("data.task.title").description("Title of the task"),
                        fieldWithPath("data.task.creatorId").description("ID of the user who created the task"),
                        fieldWithPath("data.task.archived").description("Indicates whether the task is archived"),
                        fieldWithPath("data.task.createdAt").description("Task creation timestamp"),
                        fieldWithPath("data.task.updatedAt").description("Task last update timestamp"),
                    )
                )
            )
    }
}