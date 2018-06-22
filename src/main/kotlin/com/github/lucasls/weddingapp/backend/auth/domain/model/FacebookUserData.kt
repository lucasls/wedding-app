package com.github.lucasls.weddingapp.backend.auth.domain.model

data class FacebookUserData(
    val id: String,
    val name: String,
    val email: String,
    val friendsIds: List<String>
)