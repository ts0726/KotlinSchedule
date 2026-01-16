package kmp.project.schedule.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SseHello(
    @SerialName("sessionId")
    val sessionId: String
)

@Serializable
data class SchedulesToDelete(
    @SerialName("uuids")
    val uuids: List<String>
)