package com.github.lucasls.weddingapp.test.helper

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import org.junit.After
import org.junit.Before
import org.junit.experimental.categories.Category
import java.net.ServerSocket
import java.net.URL

@Category(IntegrationTest::class)
open class DatastoreITestBase {
    lateinit var datastore: Datastore
    lateinit var process: Process

    @Before
    fun _DatastoreITestBaseSetUp() {
        //val socket = ServerSocket(8081)

        val port = 8081
        process = ProcessBuilder("/opt/google-cloud/google-cloud-sdk/bin/gcloud", "beta", "emulators", "datastore", "start", "--no-store-on-disk", "--host-port=localhost:$port")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        do {
            val len = try {
                val connection = URL("http://localhost:$port/").openConnection()
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.connect()
                connection.contentLength
            } catch (e: Exception) {
                Thread.sleep(1000)
                -1
            }
        } while (len == -1)


        datastore = DatastoreOptions.getDefaultInstance()
            .toBuilder()
            .setHost("http://localhost:$port")
            .build()
            .service
    }

    @After
    fun _DatastoreITestBaseTearDown() {
        process.destroy()
        process.waitFor()

        do {
            val len = try {
                val connection = URL("http://localhost:8081/").openConnection()
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.connect()
                connection.contentLength
            } catch (e: Exception) {
                Thread.sleep(1000)
                -1
            }
        } while (len != -1)
    }
}