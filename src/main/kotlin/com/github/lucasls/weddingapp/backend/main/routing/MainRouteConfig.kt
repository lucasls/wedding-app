package com.github.lucasls.weddingapp.backend.main.routing

import com.github.lucasls.weddingapp.backend.auth.routing.AuthRouteConfig
import com.github.lucasls.weddingapp.backend.guests.GuestsWebAdapter
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRouteConfig @Inject constructor(
    private val authRouteConfig: AuthRouteConfig,
    private val guestsWebAdapter: GuestsWebAdapter
) {

    fun configure(routing: Routing) = with(routing) {
        post("/auth-providers/facebook:authenticate") {
            authRouteConfig.apply { call.authenticateFacebook() }
        }

        get("/families/{familyCode}") {
            guestsWebAdapter.getFamily(call)
        }

        post("/families/{familyCode}/members/{memberCode}/going") {
            guestsWebAdapter.setMemberGoing(call)
        }

        get("/guests/who-is-going") {
            guestsWebAdapter.whoIsGoing(call)
        }
    }

}