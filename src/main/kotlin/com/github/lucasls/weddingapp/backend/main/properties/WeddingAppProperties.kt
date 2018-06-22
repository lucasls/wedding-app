package com.github.lucasls.weddingapp.backend.main.properties

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeddingAppProperties @Inject constructor() {

    class Facebook {
        val appId = "375557046297989"
        val appSecret = "74b2172d12a2fbd2f0ad7dadd9a0c101"
    }
    val facebook = Facebook()

}