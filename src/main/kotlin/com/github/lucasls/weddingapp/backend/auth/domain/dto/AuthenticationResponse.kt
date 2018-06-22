package com.github.lucasls.weddingapp.backend.auth.domain.dto

data class AuthenticationResponse(
    val status: Status,
    val accessToken: String? = null,
    val refreshToken: String? = null
) {
    enum class Status {
        Connected, MissingPermissions, Error
    }
}