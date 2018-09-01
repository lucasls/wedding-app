package com.github.lucasls.weddingapp.backend.auth.domain.repository

import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newEntity
import com.github.lucasls.weddingapp.backend.main.helper.datastore.newKeyFactory
import com.github.lucasls.weddingapp.backend.main.helper.datastore.queryOne
import com.github.lucasls.weddingapp.backend.main.helper.datastore.setValue
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StringValue
import com.google.cloud.datastore.Value
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val datastore: Datastore
) {

    private val keyFactory by lazy { datastore.newKeyFactory("User") }

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
            setValue("email", email)
            setValue("facebookId", facebookId)
            setValue("familyId", familyId)
            setValue("facebookAccessToken", facebookAccessToken)
            setValue("facebookFriendsIds", facebookFriendsIds)
        }

    }

    fun findByEmail(email: String): User.Data? {
        val entity = datastore.queryOne("select * from User where email=@email") {
            setBinding("email", email)
        }

        return entity?.toUserData()
    }

    fun save(userData: User.Data) {
        datastore.put(userData.toEntity())
    }

}