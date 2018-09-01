package com.github.lucasls.weddingapp.backend

import com.github.lucasls.weddingapp.backend.main.dependency.DaggerWeddingAppDIComponent
import com.github.lucasls.weddingapp.backend.main.dependency.KtorApplicationDIModule
import com.github.lucasls.weddingapp.backend.main.routing.MainRouteConfig
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeddingAppConfig @Inject constructor(
    val ktorApplication: Application,
    val mainRouteConfig: MainRouteConfig
) {
    fun configure() = with(ktorApplication) {

        install(ContentNegotiation) {
            gson {}
        }

        install(CORS) {
            header(HttpHeaders.XForwardedProto)
            header(HttpHeaders.XForwardedFor)
            host("lucasenicole.com.br", listOf("http", "https"), listOf("www", "dev", "www.dev"))
        }

        routing {
            mainRouteConfig.configure(this)
        }
    }
}

fun ktorModule(ktorApplication: Application) {
    val weddingAppDIComponent = DaggerWeddingAppDIComponent.builder()
        .ktorApplicationDIModule(KtorApplicationDIModule(ktorApplication))
        .build()

    weddingAppDIComponent.weddingAppConfig().configure()
}