package com.github.lucasls.weddingapp.backend.auth.service

import com.github.lucasls.weddingapp.backend.auth.domain.dto.FacebookAuthenticationRequest
import com.github.lucasls.weddingapp.backend.auth.domain.model.FacebookUserData
import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.auth.domain.model.UserPermission
import com.github.lucasls.weddingapp.backend.auth.domain.repository.FacebookAuthRepository
import com.github.lucasls.weddingapp.backend.auth.domain.repository.UserRepository
import com.github.lucasls.weddingapp.backend.auth.domain.service.AuthenticationService
import com.github.lucasls.weddingapp.backend.auth.domain.service.AuthorizationService
import com.github.lucasls.weddingapp.backend.main.repository.ConfigRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class AuthenticationServiceTest {

    @Test
    fun `test authenticate with facebook`(): Unit = runBlocking<Unit> {

        val facebookAuthRepository = mock<FacebookAuthRepository> {

            onBlocking { it.exchangeToken(any()) }
                .thenReturn("long-123456789")

            onBlocking { it.permissions(any()) }
                .thenReturn(setOf("email", "user_friends", "public_profile"))

            onBlocking { it.userData(any()) }
                .thenReturn(FacebookUserData(
                    id = "fbid-12345",
                    name = "Lucas",
                    email = "lls.lucas@gmail.com",
                    friendsIds = emptyList()
                ))
        }

        val userRepository = mock<UserRepository> {  }

        val authorizationService = mock<AuthorizationService> {
            onBlocking { with(it) { any<User>().getPermissions() } }
                .thenReturn(setOf(UserPermission.CanPay))
        }

        val configRepository = mock<ConfigRepository> {
            on { it.get<String>("jwtKey") }
                .thenReturn("jwtKey-12345")
        }

        val authenticationService = AuthenticationService(
            facebookAuthRepository = facebookAuthRepository,
            userRepository = userRepository,
            authorizationService = authorizationService,
            configRepository = configRepository
        )

        authenticationService.authenticateFacebook(FacebookAuthenticationRequest("short-123456789"))
    }

}