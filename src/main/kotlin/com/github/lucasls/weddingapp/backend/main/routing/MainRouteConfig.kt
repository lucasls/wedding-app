package com.github.lucasls.weddingapp.backend.main.routing

import com.github.lucasls.weddingapp.backend.auth.routing.AuthRouteConfig
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRouteConfig @Inject constructor(
    private val authRouteConfig: AuthRouteConfig
) {

    fun configure(routing: Routing) = with(routing) {
        post("/auth-providers/facebook:authenticate") {
            authRouteConfig.apply { call.authenticateFacebook() }
        }
    }

}