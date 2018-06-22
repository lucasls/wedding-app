package com.github.lucasls.weddingapp.backend.auth.routing

import com.github.lucasls.weddingapp.backend.auth.domain.dto.AuthenticationResponse
import com.github.lucasls.weddingapp.backend.auth.domain.dto.FacebookAuthenticationRequest
import com.github.lucasls.weddingapp.backend.auth.domain.service.AuthenticationService
import io.ktor.application.ApplicationCall
import io.ktor.request.receive
import io.ktor.response.respond
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRouteConfig @Inject constructor(
    private val authenticationService: AuthenticationService
) {

    data class Response(val status: AuthenticationResponse.Status)

    suspend fun ApplicationCall.authenticateFacebook() {
        val request = receive<FacebookAuthenticationRequest>()
        val (status, accessToken, refreshToken) = authenticationService.authenticateFacebook(request)

        if (status == AuthenticationResponse.Status.Connected) {
            response.cookies.append("access-token", accessToken!!, httpOnly = true)
            response.cookies.append("refresh-token", refreshToken!!, httpOnly = true)
        } else {
            response.cookies.appendExpired("access-token")
            response.cookies.appendExpired("refresh-token")
        }

        respond(Response(status))
    }

}