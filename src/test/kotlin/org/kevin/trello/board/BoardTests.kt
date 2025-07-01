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
import org.kevin.trello.board.mapper.BoardViewMapper
import org.kevin.trello.board.mapper.query.BoardViewSearchQuery
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
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    val boardViewMapper: BoardViewMapper,
) {
    private val email = "${RandomString(8).nextString()}@example.com"
    private val nickname = "${RandomString(4).nextString()}-user"
    private val password = "Password123!"

    private lateinit var accountUid: String
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

    @Test
    @DisplayName("view board list")
    fun `view board list`() {
        for (i in 1..5) {
            val boardName = "test board $i"
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
        }

        mockMvc.perform(
            get("/api/v1/board")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.boards").exists())
            .andDo(
                document(
                    "view-board-list",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("access token cookie")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("data.boards").description("List of boards"),
                        fieldWithPath("data.boards[].boardId").description("Board unique identifier"),
                        fieldWithPath("data.boards[].name").description("Name of the board"),
                        fieldWithPath("data.boards[].visibility").description("Visibility of the board"),
                        fieldWithPath("data.boards[].uid").description("user ID"),
                        fieldWithPath("data.boards[].readOnly").description("Whether the board is read-only"),
                        fieldWithPath("data.boards[].addedAt").description("Board creation timestamp"),
                        fieldWithPath("data.boards[].lastOpen").description("Last opened timestamp"),
                        fieldWithPath("data.boards[].isFavorite").description("Whether the board is marked as favorite"),
                        fieldWithPath("data.hasNext").description("Whether there are more boards to fetch"),
                        fieldWithPath("data.hasPrevious").description("Whether there are previous boards to fetch"),
                        fieldWithPath("data.total").description("Total number of boards available"),
                        fieldWithPath("data.offset").description("Current offset for pagination"),
                        fieldWithPath("data.limit").description("Number of boards returned in this response"),
                    )
                )
            )
    }

    @Test
    @DisplayName("like and dislike board")
    fun `like and dislike board`() {
        val boardName = "test board"
        val requestBody = """
                {
                    "name": "$boardName",
                    "visibility": "PRIVATE"
                }
            """.trimIndent()

        val boardId = mockMvc.perform(
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
                str
            }

        mockMvc.perform(
            post("/api/v1/board/$boardId/like")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "like-board",
                    responseFields(
                        fieldWithPath("code").description("Response code"),

                    ),
                )
            )

        BoardViewSearchQuery(
            uid = accountUid,
            boardId = boardId,
        ).let {
            boardViewMapper.searchBoardView(it)
                .firstOrNull()
                ?.let { boardView ->
                    assertEquals(true, boardView.isFavorite, "Board should be marked as favorite after liking")
                } ?: throw IllegalStateException("Board not found after liking")
        }

        mockMvc.perform(
            post("/api/v1/board/$boardId/dislike")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "dislike-board",
                    responseFields(
                        fieldWithPath("code").description("Response code"),

                        ),
                )
            )

        BoardViewSearchQuery(
            uid = accountUid,
            boardId = boardId,
        ).let {
            boardViewMapper.searchBoardView(it)
                .firstOrNull()
                ?.let { boardView ->
                    assertEquals(false, boardView.isFavorite, "Board should be unmarked after disliking")
                } ?: throw IllegalStateException("Board not found after liking")
        }
    }

    @Test
    fun `read board content`() {
        val boardName = "test board"
        val boardId = """
            {
                "name": "$boardName",
                "visibility": "PRIVATE"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/board")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
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
                        str
                    }
            }

        """
            {
                "name": "1",
                "boardId": "$boardId"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/tasklist")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
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
                        str
                    }
            }
            .let {
                for (i in 1..5) {
                    """
                        {
                            "title": "Task $i",
                            "listId": "$it"
                        }
                    """.trimIndent()
                        .let {
                            mockMvc.perform(
                                post("/api/v1/task")
                                    .cookie(accessCookie)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(it)
                            )
                                .andExpect(status().isOk)
                                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                                .andExpect(jsonPath("$.data.task").exists())
                        }
                }
            }

        mockMvc.perform(
            get("/api/v1/board/{boardId}", boardId)
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.content").exists())
            .andExpect(jsonPath("$.data.content.id").value(boardId))
            .andDo(
                document(
                    "read-board-content",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("access token cookie")
                    ),
                    pathParameters(
                        parameterWithName("boardId").description("The unique identifier of the board to read")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("data.content").description("Board contents"),
                        fieldWithPath("data.content.id").description("Board unique identifier"),
                        fieldWithPath("data.content.uid").description("user ID"),
                        fieldWithPath("data.content.name").description("Name of the board"),
                        fieldWithPath("data.content.visibility").description("Visibility of the board"),
                        fieldWithPath("data.content.readOnly").description("Whether the board is read-only"),
                        fieldWithPath("data.content.isFavorite").description("Whether the board is marked as favorite"),
                        fieldWithPath("data.content.lists").optional().description("List of task lists in the board"),
                        fieldWithPath("data.content.lists[].id").description("Task list unique identifier"),
                        fieldWithPath("data.content.lists[].boardId").description("Board ID for the list"),
                        fieldWithPath("data.content.lists[].name").description("Name of the task list"),
                        fieldWithPath("data.content.lists[].position").description("Position of the task list"),
                        fieldWithPath("data.content.lists[].tasks").optional().description("Tasks in the list"),
                        fieldWithPath("data.content.lists[].tasks[].id").description("Task unique identifier"),
                        fieldWithPath("data.content.lists[].tasks[].listId").description("List ID for the task"),
                        fieldWithPath("data.content.lists[].tasks[].finished").description("Whether the task is finished"),
                        fieldWithPath("data.content.lists[].tasks[].position").description("Position of the task"),
                        fieldWithPath("data.content.lists[].tasks[].title").description("Title of the task"),
                        fieldWithPath("data.content.lists[].tasks[].description").description("Description of the task"),
                        fieldWithPath("data.content.lists[].tasks[].date").description("Date of the task"),
                    )
                )
            )
    }
}