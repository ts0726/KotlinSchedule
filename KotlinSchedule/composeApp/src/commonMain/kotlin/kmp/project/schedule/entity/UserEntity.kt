package kmp.project.schedule.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterEntity(
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String,
    @SerialName("nickname")
    val nickname: String
)

@Serializable
data class LoginEntity(
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)
