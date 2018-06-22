package com.github.lucasls.weddingapp.backend.auth.service

import com.github.lucasls.weddingapp.backend.auth.domain.service.AuthenticationService
import com.github.lucasls.weddingapp.backend.auth.domain.dto.FacebookAuthenticationRequest
import com.github.lucasls.weddingapp.backend.auth.domain.repository.FacebookAuthRepository
import com.github.lucasls.weddingapp.backend.auth.domain.model.FacebookUserData
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AuthenticationServiceTest {

    @Mock
    lateinit var facebookAuthRepository: FacebookAuthRepository
    @InjectMocks
    lateinit var authenticationService: AuthenticationService

    @Before
    fun setUp() {
    }

    @Test
    fun authenticateFacebook() {
        runBlocking {
            stubbing(facebookAuthRepository) {
                onBlocking { exchangeToken(any()) }.thenReturn("long-${UUID.randomUUID()}")

                onBlocking { permissions(any()) }.thenReturn(setOf("email", "user_friends", "public_profile"))

                onBlocking { userData(any()) }.thenReturn(FacebookUserData(
                    "1234", "Matheus Akira", "matheus.akira@gmail.com", listOf("4321")
                ))
            }

            val authenticationResponse = authenticationService.authenticateFacebook(FacebookAuthenticationRequest(
                "short-${UUID.randomUUID()}"
            ))

            assertNotNull(authenticationResponse)

        }
    }

}