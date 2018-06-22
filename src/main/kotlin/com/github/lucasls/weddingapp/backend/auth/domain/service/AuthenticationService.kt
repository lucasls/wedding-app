package com.github.lucasls.weddingapp.backend.auth.domain.service

import com.github.lucasls.weddingapp.backend.auth.domain.dto.AuthenticationResponse
import com.github.lucasls.weddingapp.backend.auth.domain.dto.FacebookAuthenticationRequest
import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.auth.domain.repository.FacebookAuthRepository
import com.github.lucasls.weddingapp.backend.auth.domain.repository.UserRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationService @Inject constructor(
    private val facebookAuthRepository: FacebookAuthRepository,
    private val userRepository: UserRepository,
    private val authorizationService: AuthorizationService
) {
    companion object {
        private val MANDATORY_PERMISSIONS = setOf("email", "user_friends", "public_profile")
    }

    suspend fun authenticateFacebook(request: FacebookAuthenticationRequest): AuthenticationResponse {

        val longLivedToken = facebookAuthRepository.exchangeToken(request.accessToken)
            ?: return AuthenticationResponse(AuthenticationResponse.Status.Error)

        val facebookPermissions = facebookAuthRepository.permissions(longLivedToken)

        if (!facebookPermissions.containsAll(MANDATORY_PERMISSIONS)) {
            return AuthenticationResponse(AuthenticationResponse.Status.MissingPermissions)
        }

        val facebookUserData = facebookAuthRepository.userData(longLivedToken)

        val existingUserData = userRepository.findByEmail(facebookUserData.email)

        val userData = when (existingUserData) {
            null -> {
                User.Data(
                    id = UUID.randomUUID().toString(),
                    email = facebookUserData.email,
                    facebookFriendsIds = facebookUserData.friendsIds,
                    facebookId = facebookUserData.id,
                    facebookAccessToken = longLivedToken,
                    familyId = null
                )
            }
            else -> {
                existingUserData.copy(
                    facebookId = facebookUserData.id,
                    facebookFriendsIds = facebookUserData.friendsIds,
                    facebookAccessToken = longLivedToken
                )
            }
        }

        userRepository.save(userData)
        val user = User(userData)

        val permissions = with(authorizationService) { user.getPermissions() }

        return AuthenticationResponse(
            status = AuthenticationResponse.Status.Connected,
            accessToken = UUID.randomUUID().toString(),
            refreshToken = UUID.randomUUID().toString()
        )
    }


}