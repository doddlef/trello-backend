package org.kevin.trello.auth

import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello.auth.mapper.EmailActiveTokenMapper
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class EmailPasswordRegisterTests @Autowired constructor(
    val mockMvc: MockMvc,
    val emailActiveTokenMapper: EmailActiveTokenMapper,
    val authProperties: AuthProperties
) {

    @Test
    @DisplayName("Register with email and password")
    fun `email register`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        val registerRequest = """
            {
                "email": "$email",
                "nickname": "$nickname",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.accountUid").exists())
            .andDo(
                document(
                    "email-register",
                    requestFields(
                        fieldWithPath("email").description("primary email address of the account"),
                        fieldWithPath("password").description("password for the account, at least 8 characters long, contains at least one letter and one number"),
                        fieldWithPath("nickname").description("nickname for the account, up to 32 characters long")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                        fieldWithPath("data.accountUid").description("The account ID of the account")
                    )
                )
            )

        // retrieve the active token from the database
        val token = emailActiveTokenMapper.findByEmail(email)
        assert(token != null) { "Active token should be generated for the email" }

        val activeRequest = """
            {
                "token": "${token!!.token}"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/auth/active")
                .contentType(MediaType.APPLICATION_JSON)
                .content(activeRequest)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
            .andExpect( cookie().exists(authProperties.accessCookieName))
            .andExpect(cookie().exists(authProperties.refreshCookieName))
            .andDo(
                document(
                    "email-active",
                    requestFields(
                        fieldWithPath("token").description("Active token for email verification"),
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
    @DisplayName("register with invalid email")
    fun `email invalid`() {
        val email = "invalid-email"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        val registerRequest = """
            {
                "email": "$email",
                "nickname": "$nickname",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(ResponseCode.BAD_ARGUMENT.code))
            .andDo(
                document(
                    "register with invalid email",
                    requestFields(
                        fieldWithPath("email").description("primary email address of the account"),
                        fieldWithPath("password").description("password for the account, at least 8 characters long, contains at least one letter and one number"),
                        fieldWithPath("nickname").description("nickname for the account, up to 32 characters long")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                    )
                )
            )
    }

    @Test
    @DisplayName("register with invalid email")
    fun `password invalid`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "-"

        val registerRequest = """
            {
                "email": "$email",
                "nickname": "$nickname",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(ResponseCode.BAD_ARGUMENT.code))
            .andDo(
                document(
                    "register with invalid password",
                    requestFields(
                        fieldWithPath("email").description("primary email address of the account"),
                        fieldWithPath("password").description("password for the account, at least 8 characters long, contains at least one letter and one number"),
                        fieldWithPath("nickname").description("nickname for the account, up to 32 characters long")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                    )
                )
            )
    }

    @Test
    @Transactional
    fun `email already registered`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        val registerRequest = """
            {
                "email": "$email",
                "nickname": "$nickname",
                "password": "$password"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(ResponseCode.BAD_ARGUMENT.code))
            .andDo(
                document(
                    "register with already registered email",
                    requestFields(
                        fieldWithPath("email").description("primary email address of the account"),
                        fieldWithPath("password").description("password for the account, at least 8 characters long, contains at least one letter and one number"),
                        fieldWithPath("nickname").description("nickname for the account, up to 32 characters long")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                    )
                )
            )
    }
}