package com.github.lucasls.weddingapp.backend.auth.domain.repository

import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.auth.domain.model.User.Data
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newEntity
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.github.lucasls.weddingapp.backend.main.helper.datastore.queryOne
import com.google.cloud.datastore.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val datastore: Datastore
) {

    private val keyFactory = datastore.newKeyFactory("User") {
    }

    private fun Entity.toUserData() = User.Data(
        id = key.name,
        email = getString("email"),
        familyId = getString("familyId"),
        facebookId = getString("facebookId"),
        facebookFriendsIds = getList<Value<String>>("facebookFriendsIds").map { it.get() },
        facebookAccessToken = getString("facebookAccessToken")
    )

    private fun User.Data.toEntity(): Entity {
        return keyFactory.newEntity(id) {

            set("email", email)
            set("facebookId", facebookId)
            set("familyId", familyId)

            facebookFriendsIds
                .map { StringValue.of(it) }
                .let { set("facebookFriendsIds", it) }
        }

    }

    fun findByEmail(email: String): User.Data? {
        val entity = datastore.queryOne("select * from user where email=@email") {
            setBinding("email", email)
        }

        return entity?.toUserData()
    }

    fun save(userData: User.Data) {
        datastore.put(userData.toEntity())
    }

}