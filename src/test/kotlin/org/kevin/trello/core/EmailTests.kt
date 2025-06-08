package org.kevin.trello.core

import org.junit.jupiter.api.Test
import org.kevin.trello.core.service.impl.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EmailTests @Autowired constructor (
    private val emailService: EmailService,
) {

    @Test
    fun `test send email`() {
        emailService.sendEmail(
            "doddlefeng@gmail.com",
            "test",
            "This is a test email from Trello Kotlin project."
        )
    }
}