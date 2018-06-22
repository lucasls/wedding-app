package com.github.lucasls.weddingapp.backend.auth.domain.repository

import com.github.lucasls.weddingapp.backend.auth.domain.model.FacebookUserData
import com.github.lucasls.weddingapp.backend.main.properties.WeddingAppProperties
import com.google.gson.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.takeFrom
import io.ktor.util.url
import mu.KotlinLogging
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson

@Singleton
class FacebookAuthRepository @Inject constructor(
    properties: WeddingAppProperties,
    @Named("facebookHttpClient")
    private val httpClient: HttpClient,
    @Named("facebookGson")
    private val gson: Gson
) {

    private val log = KotlinLogging.logger {}

    private val appId = properties.facebook.appId
    private val appSecret = properties.facebook.appSecret

    suspend fun appToken(): String {
        val res: JsonObject = httpClient.get(
            "https://graph.facebook.com/oauth/access_token?client_id=$appId&client_secret=$appSecret&grant_type=client_credentials")

        return res["access_token"].asString
    }

    suspend fun exchangeToken(accessToken: String): String? {

        val res: JsonObject = httpClient.get(url {
            takeFrom("https://graph.facebook.com/oauth/access_token")

            parameters.append("client_id", appId)
            parameters.append("client_secret", appSecret)
            parameters.append("grant_type", "fb_exchange_token")
            parameters.append("fb_exchange_token", accessToken)
        })

        if (res["error"] != null) {
            log.warn("Error exchanging token: ${res["error"]}")
            return null
        }

        return res["access_token"].string
    }

    suspend fun permissions(accessToken: String): Set<String> {
        val res: JsonObject = httpClient.get(url {
            takeFrom("https://graph.facebook.com/me/permissions")
            parameters.append("access_token", accessToken)
        })

        return res["data"].array
            .filter { it["status"].string == "granted" }
            .map { it["permission"].string }
            .toSet()
    }

    suspend fun userData(accessToken: String): FacebookUserData {
        val res: JsonObject = httpClient.get(url {
            takeFrom("https://graph.facebook.com/me")

            parameters.append("access_token", accessToken)
            parameters.append("fields", "id,name,email,friends")
        })

        return gson.fromJson<FacebookUserData>(res)
            .copy(friendsIds = res["friends"]["data"].array.map { it.asString })
    }

}