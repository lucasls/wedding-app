package com.github.lucasls.weddingapp.backend.auth.domain.model

data class User(
    val id: String,
    val data: Data? = null
) {

    constructor(data: Data) : this(data.id, data)

    data class Data(
        val id: String,
        val email: String,
        val familyId: String?,
        val facebookId: String,
        val facebookFriendsIds: List<String> = emptyList(),
        val facebookAccessToken: String
    )

}