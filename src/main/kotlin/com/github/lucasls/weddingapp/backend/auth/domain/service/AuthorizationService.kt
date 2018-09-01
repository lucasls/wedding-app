package com.github.lucasls.weddingapp.backend.auth.domain.service

import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.auth.domain.model.UserPermission
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizationService @Inject constructor() {

    companion object {
        val allowedFacebookFriends = setOf("TODEFINE1", "TODEFINE2") //FIXME get real IDS
    }

    private fun UserPermission.allowedFor(userData: User.Data): Boolean {
        return when (this) {
            UserPermission.CanConfirmAttendance -> userData.familyId != null
            UserPermission.CanPay -> userData.familyId != null || userData.facebookFriendsIds.any { it in allowedFacebookFriends }
        }
    }

    fun User.getPermissions(): Set<UserPermission> {
        this.data ?: throw IllegalStateException("Must have data")

        return UserPermission.values()
            .filter { it.allowedFor(this.data) }
            .toSet()
    }


}