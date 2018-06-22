package com.github.lucasls.weddingapp.backend.helloworld.service

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HelloWorldService @Inject constructor() {

    fun hello(): Map<String, Any> {
        val x = mapOf(
            "name" to "Lucas",
            "age" to 28
        )
        return x
    }
}