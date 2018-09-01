package com.github.lucasls.weddingapp.backend.main.repository

import com.github.lucasls.weddingapp.backend.main.helper.datastore.newEntity
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val datastore: Datastore
) {

    private val globalKey by lazy { datastore.newKeyFactory("Config").newKey(1) }

    private fun entity() = datastore[globalKey] ?: defaultConfig().also { datastore.put(it) }

    fun <T: Any> get(key: String, getter: Entity.(String) -> T?): T? {

        return entity()
            .takeIf { it.contains(key) }
            ?.let { getter.invoke(it, key) }
    }

    inline operator fun <reified T> get(key: String): T? {
        return when(T::class) {
            String::class -> get(key, Entity::getString) as T?
            Long::class -> get(key, Entity::getLong) as T?
            else -> null
        }
    }

    private fun defaultConfig(): Entity {
        return globalKey.newEntity {
            set("facebookAppSecret", "74***************************101")
            set("facebookAppId", "37***********89")
            set("jwtKey", "test-key")
        }
    }

}