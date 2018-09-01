package com.github.lucasls.weddingapp.backend.main.dependency

import com.github.lucasls.weddingapp.backend.WeddingAppConfig
import com.google.cloud.datastore.DatastoreOptions
import com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Component
import dagger.Module
import dagger.Provides
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import javax.inject.Named
import javax.inject.Singleton

private fun GsonBuilder.setFacebookSettings() {
    setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
}

@Module
class KtorApplicationDIModule(private val ktorApplication: Application) {
    @Provides
    fun ktorApplication() = ktorApplication
}

@Module
class GsonDIModule {
    @Provides
    @Singleton
    fun gson() = Gson()
}

@Module
class HttpClientDIModule {
    @Provides
    @Singleton
    fun httpClient() = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
}

@Module
class DatastoreDIModule {
    @Provides
    @Singleton
    fun datatore() = DatastoreOptions.getDefaultInstance().getService();
}

@Singleton
@Component(modules = [
    KtorApplicationDIModule::class,
    HttpClientDIModule::class,
    GsonDIModule::class,
    DatastoreDIModule::class
])
interface WeddingAppDIComponent {
    fun weddingAppConfig(): WeddingAppConfig
}
