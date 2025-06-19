package org.kevin.trello.account

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello.core.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class AccountInfoTests @Autowired constructor(
    val mockMvc: MockMvc,
) {

    @Test
    @DisplayName("get account info without login")
    fun `get account info without login`() {
        mockMvc.perform(
            get("/api/account/me")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ResponseCode.NEED_LOGIN.code))
            .andDo(
                document(
                    "account-info-unauthorized",
                    responseFields(
                        fieldWithPath("code").description("Response code indicating the need for login"),
                        fieldWithPath("message").description("Message indicating that the user has not logged in")
                    )
                )
            )
    }
}