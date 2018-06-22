package com.github.lucasls.weddingapp.backend

import com.github.lucasls.weddingapp.backend.main.dependency.DaggerDependencyResolver
import com.github.lucasls.weddingapp.backend.main.dependency.KtorApplicationProvider
import com.github.lucasls.weddingapp.backend.main.routing.MainRouteConfig
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.*
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

        routing {
            mainRouteConfig.configure(this)
        }
    }
}

fun ktorModule(ktorApplication: Application) {
    val dependencyResolver = DaggerDependencyResolver.builder()
        .ktorApplicationProvider(KtorApplicationProvider(ktorApplication))
        .build()

    dependencyResolver.weddingAppConfig().configure()
}