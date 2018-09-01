package com.github.lucasls.weddingapp.backend.main.routing

import com.github.lucasls.weddingapp.backend.guests.GuestsWebAdapter
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRouteConfig @Inject constructor(
    private val guestsWebAdapter: GuestsWebAdapter
) {

    fun configure(routing: Routing) = with(routing) {
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