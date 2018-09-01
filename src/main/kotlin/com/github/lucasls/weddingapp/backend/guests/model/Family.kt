package com.github.lucasls.weddingapp.backend.guests.model

data class Family(
    val code: String,
    val name: String,
    val message: String,
    val single: Boolean,
    val members: List<Member>
)

data class Member(
    val code: String,
    val name: String,
    val going: Boolean?
)