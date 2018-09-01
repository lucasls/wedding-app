package com.github.lucasls.weddingapp.backend.guests

import com.github.lucasls.weddingapp.backend.guests.model.Family
import com.github.lucasls.weddingapp.backend.guests.model.Member
import com.github.lucasls.weddingapp.backend.main.helper.datastore.getBooleanOrNull
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.github.lucasls.weddingapp.backend.main.helper.datastore.query
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.experimental.async
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestsRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val datastore: Datastore
) {

    private val keyFactory by lazy { datastore.newKeyFactory("FamilyMemberGoing") }

    var cacheFamilies: Map<String, JsonElement> = emptyMap()
    var cacheMembers: Map<String, List<JsonElement>> = emptyMap()
    var cacheLastUpdate = Instant.EPOCH

    private suspend fun refreshCaches() {
        val jsonMembers = async { httpClient.get<JsonObject>("https://sheets.googleapis.com/v4/spreadsheets/1CPJUd9fS_zZ1Ku29Lg3K1lw70HVY3SOhiqZdWhAzAuo/values/members?key=AI********************************G3T7s") }
        val jsonFamilies = async { httpClient.get<JsonObject>("https://sheets.googleapis.com/v4/spreadsheets/1CPJUd9fS_zZ1Ku29Lg3K1lw70HVY3SOhiqZdWhAzAuo/values/families?key=AI********************************G3T7s") }

        cacheMembers = jsonMembers.await()["values"].array
            .drop(1)
            .groupBy({ it[0].string }, { it })

        cacheFamilies = jsonFamilies.await()["values"].array
            .drop(1)
            .map { it[0].string to it }
            .toMap()

        cacheLastUpdate = Instant.now()
    }

    private fun getMembers(familyCode: String, attendanceMap: Map<String, Boolean?>): List<Member> {



        return cacheMembers[familyCode]?.map {
            val memberCode = it[1].string
            Member(
                code = memberCode,
                name = it[2].string,
                going = attendanceMap["$familyCode:$memberCode"]
            )
        } ?: emptyList()
    }

    private fun getFamily(it: JsonElement, attendanceMap: Map<String, Boolean?>): Family {
        val familyCode = it[0].string
        val familyName = it[1].string
        val familyMessage = it[2].string

        return Family(
            code = familyCode,
            name = familyName,
            message = familyMessage.takeUnless { it.isEmpty() } ?: familyName,
            members = getMembers(familyCode, attendanceMap),
            single = it[3].string == "TRUE"
        )
    }

    suspend fun findFamily(code: String): Family? {
        if (Instant.now() - Duration.ofMinutes(1) >= cacheLastUpdate) {
            refreshCaches()
        }

        val familyAttendance = datastore.query("select * from FamilyMemberGoing where familyCode=@familyCode") {
            setBinding("familyCode", code)
        }

        val attendanceMap = familyAttendance
            .map { it.key.name to it.getBooleanOrNull("going") }
            .toMap()

        return cacheFamilies[code]?.let { getFamily(it, attendanceMap) }
    }

    fun setMemberGoing(familyCode: String, memberCode: String, going: Boolean?) {
        val key = keyFactory.newKey("$familyCode:$memberCode")
        if (going == null) {
            datastore.delete(key)
        } else {
            val entityBuilder = datastore[key]
                ?.let { Entity.newBuilder(it) }
                ?: Entity.newBuilder(key)
                    .set("familyCode", familyCode)

            entityBuilder.set("going", going)
            datastore.put(entityBuilder.build())
        }
    }

    suspend fun allFamiliesGoing(): List<Family> {
        if (Instant.now() - Duration.ofMinutes(1) >= cacheLastUpdate) {
            refreshCaches()
        }

        val allAttendencies = datastore.query("select * from FamilyMemberGoing where going=@going") {
            setBinding("going", true)
        }
        val attendanceMap = allAttendencies
            .map { it.key.name to it.getBooleanOrNull("going") }
            .toMap()

        return cacheFamilies.values
            .map { getFamily(it, attendanceMap) }
    }
}