package kmp.project.schedule.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleEntity(
    @SerialName("uuid")
    val uuid: Int,
    @SerialName("userName")
    val userName: String,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("date")
    val date: Long,
    @SerialName("repeatMode")
    val repeatMode: RepeatMode,
    @SerialName("location")
    val location: String,
    @SerialName("sequence")
    val sequence: Int
)

enum class RepeatMode {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
