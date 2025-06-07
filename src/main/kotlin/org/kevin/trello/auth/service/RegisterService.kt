package org.kevin.trello.auth.service

import org.kevin.trello.auth.service.vo.EmailRegisterVO

interface RegisterService {
    /**
     * Registers a new user with the provided email and password.
     * The created account should not be active until the user verifies their email address.
     *
     * @param vo The value object containing the registration details.
     */
    fun emailRegister(vo: EmailRegisterVO)

    fun verificationEmail(token: String)
}