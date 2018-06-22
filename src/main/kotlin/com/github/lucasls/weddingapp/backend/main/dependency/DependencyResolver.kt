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
class KtorApplicationProvider(private val ktorApplication: Application) {
    @Provides
    fun ktorApplication() = ktorApplication
}

@Module
class GsonProvider {
    @Provides
    @Singleton
    fun gson() = Gson()

    @Provides
    @Singleton
    @Named("facebookGson")
    fun facebookGson() = GsonBuilder().apply {
        setFacebookSettings()
    }.create()
}

@Module
class HttpClientProvider {
    @Provides
    @Singleton
    @Named("facebookHttpClient")
    fun facebookHttpClient() = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                setFacebookSettings()
            }
        }
    }

    @Provides
    @Singleton
    fun httpClient() = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
}

@Module
class DatastoreProvider {
    @Provides
    @Singleton
    fun datatore() = DatastoreOptions.getDefaultInstance().getService();
}

@Singleton
@Component(modules = [
    KtorApplicationProvider::class,
    HttpClientProvider::class,
    GsonProvider::class,
    DatastoreProvider::class
])
interface DependencyResolver {
    fun weddingAppConfig(): WeddingAppConfig
}
