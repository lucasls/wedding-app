package com.github.lucasls.weddingapp.backend.auth.service

import com.github.lucasls.weddingapp.backend.main.helper.datastore.get
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newEntity
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newGqlQuery
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.cloud.datastore.DatastoreOptions
import org.junit.After
import org.junit.Before
import org.junit.Test


class DatastoreITest {
    private val helper = LocalServiceTestHelper(LocalDatastoreServiceTestConfig())

    @Before
    fun setUp() {
        helper.setUp()
    }

    @After
    fun tearDown() {
        helper.tearDown()
    }

    @Test
    fun name() {

        val datastore = DatastoreOptions.getDefaultInstance().service

        val keyFactory = datastore.newKeyFactory("Person")
        val key = keyFactory.newKey("john.doe@gmail.com")

        val entity = key.newEntity {
            set("name", "John Doe")
            set("age", 51)
            set("favorite_food", "pizza")
        }

        datastore.put(entity)

        val johnEntity = datastore.get(keyFactory, "john.doe@gmail.com")

        println(johnEntity)

        val query = newGqlQuery("select * from Person where age > @age") {
            setBinding("age", 40)
        }

        datastore.run(query).forEach {
            println(it)
        }
    }
}