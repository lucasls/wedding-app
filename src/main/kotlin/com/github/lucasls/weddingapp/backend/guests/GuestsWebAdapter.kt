package com.github.lucasls.weddingapp.backend.guests

import com.fasterxml.jackson.databind.JsonNode
import com.github.lucasls.weddingapp.backend.guests.model.Family
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestsWebAdapter @Inject constructor(
    private val guestsRepository: GuestsRepository
) {


    data class GetFamilyResponse(
        val family: Family?
    )

    data class WhoIsGoingResponse(
        val groups: List<Group>
    ) {
        data class Group(
            val name: String,
            val guests: List<Guest>
        )

        data class Guest(
            val name: String
        )
    }

    suspend fun getFamily(call: ApplicationCall) {

        val code = call.parameters["familyCode"]!!

        val family = guestsRepository.findFamily(code)

        call.respond(GetFamilyResponse(
            family = family
        ))
    }

    suspend fun setMemberGoing(call: ApplicationCall) {
        val familyCode = call.parameters["familyCode"]!!
        val memberCode = call.parameters["memberCode"]!!

        val going = call.receive<String>()
            .let {
                when (it) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            }

        guestsRepository.setMemberGoing(familyCode, memberCode, going)
        call.response.status(HttpStatusCode.OK)
    }

    suspend fun whoIsGoing(call: ApplicationCall) {
        val allFamiliesGoing = guestsRepository.allFamiliesGoing()

        val familyGroups = allFamiliesGoing
            .filter { it.members.size >= 2 }
            .map { it.name to it.members }

        val othersGroup = allFamiliesGoing
            .filter { it.members.size < 2 }
            .flatMap { it.members }
            .takeUnless { it.isEmpty() }
            ?.let { "..." to it }

        val allGroups = if (othersGroup != null)
            familyGroups + othersGroup
        else
            familyGroups

        allGroups
            .map {
                WhoIsGoingResponse.Group(it.first, it.second
                    .filter { it.going == true }
                    .map { WhoIsGoingResponse.Guest(it.name) }
                )
            }
            .filter { it.guests.isNotEmpty() }
            .sortedBy { it.name }
            .let { call.respond(WhoIsGoingResponse(it)) }
    }

}