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
import org.kevin.trello.board.mapper.TaskMapper
import org.kevin.trello.board.mapper.query.TaskSearchQuery
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
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
    val taskMapper: TaskMapper,
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
                "title": "Task 2",
                "listId": "$listId"
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
            .andExpect(jsonPath("$.data.task.title").value("Task 2"))
            .andExpect(jsonPath("$.data.task.listId").value(listId))
            .andDo(
                document(
                    "create-task",
                    requestFields(
                        fieldWithPath("listId").type(String::class.java).description("ID of the list to which the task belongs"),
                        fieldWithPath("parentId").type(String::class.java).description("The parent task belongs").optional(),
                        fieldWithPath("title").type(String::class.java).description("Title of the task"),
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code indicating success or failure"),
                        fieldWithPath("message").optional().description("A message describing the result of the operation"),
                        fieldWithPath("data.task.taskId").description("Unique identifier for the task"),
                        fieldWithPath("data.task.listId").description("Unique identifier for the list to which this task belongs"),
                        fieldWithPath("data.task.finished").description("Indicates whether the task is finished"),
                        fieldWithPath("data.task.position").description("Position of the task in the list"),
                        fieldWithPath("data.task.title").description("Title of the task"),
                        fieldWithPath("data.task.description").optional().description("Description of the task"),
                        fieldWithPath("data.task.date").optional().description("Due date of the task, if applicable"),
                        fieldWithPath("data.task.creatorId").description("ID of the user who created the task"),
                        fieldWithPath("data.task.archived").description("Indicates whether the task is archived"),
                        fieldWithPath("data.task.createdAt").description("Task creation timestamp"),
                        fieldWithPath("data.task.updatedAt").description("Task last update timestamp"),
                    )
                )
            )
    }

    @Test
    fun `move task`() {
        val secondListCreateBody = """
            {
                "name": "2",
                "boardId": "$boardId"
            }
        """.trimIndent()

        val secondListId = mockMvc.perform(
            post("/api/v1/tasklist")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondListCreateBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.listId").exists())
            .andReturn()
            .response
            .contentAsString
            .let {
                val str = jacksonObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("listId")
                    .asText()
                str
            }

        val taskATitle = "A"
        val taskARequestBody = """
            {
                "title": "$taskATitle",
                "listId": "$listId"
            }
        """.trimIndent()

        val taskAId = mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskARequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value(taskATitle))
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

        val taskBTitle = "B"
        val taskBRequestBody = """
            {
                "title": "$taskBTitle",
                "listId": "$listId"
            }
        """.trimIndent()

        val taskBId = mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskBRequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value(taskBTitle))
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

        TaskSearchQuery(
            listId = listId,
        ).let {
            val tasks = taskMapper.search(it)
            assertEquals(2, tasks.size, "There should be 2 tasks in the list after creation")
            assertEquals(taskAId, tasks[0].taskId, "First task should be Task A")
            assertEquals(taskBId, tasks[1].taskId, "Second task should be Task B")
        }

        """
            {
                "taskId": "$taskAId",
                "listId": "$listId",
                "afterId": "$taskBId"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/task/move")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.newPosition").exists())
            }

        TaskSearchQuery(
            listId = listId,
        ).let {
            val tasks = taskMapper.search(it)
            assertEquals(2, tasks.size, "There should still be 2 tasks in the list after movement")
            assertEquals(taskBId, tasks[0].taskId, "First task should be Task B")
            assertEquals(taskAId, tasks[1].taskId, "Second task should be Task A")
        }

        """
            {
                "taskId": "$taskAId",
                "listId": "$secondListId",
                "afterId": null
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/task/move")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.newPosition").exists())
                    .andDo(
                        document(
                            "move-task",
                            requestFields(
                                fieldWithPath("taskId").description("the ID of the task to be moved"),
                                fieldWithPath("listId").description("ID of the destination list where the task will be moved"),
                                fieldWithPath("afterId").optional().description(
                                    "The ID of the task after which the moved task will be placed. " +
                                            "If this is null, the task will be placed at the head of the list."
                                ),
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code indicating success or failure"),
                                fieldWithPath("message").optional().description("A message describing the result of the operation"),
                                fieldWithPath("data.newPosition").description("The new position of the task in the list after movement"),
                            )
                        )
                    )
            }

        TaskSearchQuery(
            listId = listId,
        ).let {
            val tasks = taskMapper.search(it)
            assertEquals(1, tasks.size, "There should be 1 task in the first list after movement")
            assertEquals(taskBId, tasks[0].taskId, "The only task in the first list should be Task B")
        }
    }

    @Test
    @DisplayName("edit task")
    fun `edit task`() {
        val taskTitle = "Task to Edit"
        val taskRequestBody = """
            {
                "title": "$taskTitle",
                "listId": "$listId"
            }
        """.trimIndent()

        val taskId = mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value(taskTitle))
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

        val title = "Updated Task Title"
        val description = "This is an updated description."
        val date = "2023-10-01"

        """
            {
                "taskId": "$taskId",
                "title": "$title",
                "description": "$description",
                "date": "$date"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/task/edit")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andDo(
                        document(
                            "edit-task",
                            requestFields(
                                fieldWithPath("taskId").description("ID of the task to be edited"),
                                fieldWithPath("title").optional().description("New title for the task"),
                                fieldWithPath("description").optional().description("New description for the task"),
                                fieldWithPath("date").optional().description("New due date for the task, in YYYY-MM-DD format")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code indicating success or failure"),
                                fieldWithPath("message").optional().description("A message describing the result of the operation"),
                            )
                        )
                )
            }

        taskMapper.findByTaskId(taskId).let {
            assertNotNull(it, "Task should exist after editing")
            assertEquals(title, it.title, "Task title should be updated")
            assertEquals(description, it.description, "Task description should be updated")
            assertEquals(date, it.date.toString(), "Task date should be updated")
        }
    }

    @Test
    fun `finish task`() {
        val taskId = """
            {
                "title": "Task to Finish",
                "listId": "$listId"
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

            }

        mockMvc.perform(
            put("/api/v1/task/{taskId}/finish", taskId)
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "finish-task",
                    pathParameters(
                        parameterWithName("taskId").description("ID of the task to be finished")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code indicating success or failure"),
                        fieldWithPath("message").optional().description("A message describing the result of the operation"),)
                )
            )

        taskMapper.findByTaskId(taskId).let {
            assertNotNull(it, "Task should exist after finishing")
            assertEquals(true, it.finished, "Task should be marked as finished")
            assertEquals(false, it.archived, "Task should not be archived after finishing")
        }
    }

    @Test
    fun `archive task`() {
        val taskTitle = "Task to Edit"
        val taskRequestBody = """
            {
                "title": "$taskTitle",
                "listId": "$listId"
            }
        """.trimIndent()

        val taskId = mockMvc.perform(
            post("/api/v1/task")
                .cookie(accessCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.task").exists())
            .andExpect(jsonPath("$.data.task.title").value(taskTitle))
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

        mockMvc.perform(
            delete("/api/v1/task/{taskId}", taskId)
                .cookie(accessCookie!!)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andDo(
                document(
                    "archive-task",
                    pathParameters(
                        parameterWithName("taskId").description("ID of the task to be archived")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code indicating success or failure"),
                        fieldWithPath("message").optional().description("A message describing the result of the operation"),
                    )
                )
            )

        taskMapper.findByTaskId(taskId).let {
            assertNull(it, "Task should not exist after archiving")
        }
    }
}