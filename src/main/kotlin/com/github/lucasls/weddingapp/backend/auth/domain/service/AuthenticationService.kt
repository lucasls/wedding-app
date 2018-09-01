package com.github.lucasls.weddingapp.backend.auth.domain.service

import com.github.lucasls.weddingapp.backend.auth.domain.dto.AuthenticationResponse
import com.github.lucasls.weddingapp.backend.auth.domain.dto.FacebookAuthenticationRequest
import com.github.lucasls.weddingapp.backend.auth.domain.model.User
import com.github.lucasls.weddingapp.backend.auth.domain.model.UserPermission
import com.github.lucasls.weddingapp.backend.auth.domain.repository.FacebookAuthRepository
import com.github.lucasls.weddingapp.backend.auth.domain.repository.UserRepository
import com.github.lucasls.weddingapp.backend.main.repository.ConfigRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationService @Inject constructor(
    private val facebookAuthRepository: FacebookAuthRepository,
    private val userRepository: UserRepository,
    private val authorizationService: AuthorizationService,
    private val configRepository: ConfigRepository
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
        val secretKey: String = configRepository["jwtKey"] ?: throw IllegalStateException("Missing config")

        return AuthenticationResponse(
            status = AuthenticationResponse.Status.Connected,
            accessToken = createAccessToken(user.id, permissions, secretKey),
            refreshToken = createRefreshToken(user.id, secretKey)
        )
    }

    private fun createRefreshToken(userId: String, secretKey: String): String {
        return Jwts.builder().apply {
            this.setSubject(userId)
            this.setExpiration(Date.from(Instant.now() + Period.ofDays(365)))
            // Sign
            this.signWith(SignatureAlgorithm.HS512, secretKey.toByteArray())
        }.compact()
    }

    private fun createAccessToken(userId: String, permissions: Collection<UserPermission>, secretKey: String): String {
        return Jwts.builder().apply {
            this.setSubject(userId)
            this.setExpiration(Date.from(Instant.now() + Duration.ofHours(2)))
            this.claim("permissions", permissions)
            // Sign
            this.signWith(SignatureAlgorithm.HS512, secretKey.toByteArray())
        }.compact()
    }
}