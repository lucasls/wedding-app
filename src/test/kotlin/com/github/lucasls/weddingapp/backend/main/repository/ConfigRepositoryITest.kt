package com.github.lucasls.weddingapp.backend.main.repository

import com.github.lucasls.weddingapp.backend.main.helper.datastore.newEntity
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.github.lucasls.weddingapp.backend.main.helper.datastore.put
import com.github.lucasls.weddingapp.test.helper.DatastoreITestBase
import com.github.lucasls.weddingapp.test.helper.IntegrationTest
import com.google.cloud.datastore.Key
import com.nhaarman.mockitokotlin2.spy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import java.util.*

@Category(IntegrationTest::class)
class ConfigRepositoryITest : DatastoreITestBase() {
    lateinit var configRepository: ConfigRepository
    lateinit var key: Key

    @Before
    fun setUp() {
        key = datastore.newKeyFactory("Config").newKey(1)
        configRepository = spy(ConfigRepository(datastore)) {
           // on { it.defaultConfig() }.thenReturn()
        }
    }

    @Test
    fun `datastore config doesn't exist`() {
        val jwtKey: String? = configRepository["jwtKey"]
        assertEquals("test-key", jwtKey)
    }

    @Test
    fun `datastore config exists`() {

        val newJwtKey = UUID.randomUUID().toString()

        datastore.put(key.newEntity {
            set("jwtKey", newJwtKey)
        })

        val jwtKey: String? = configRepository["jwtKey"]
        assertEquals(newJwtKey, jwtKey)
    }
}