package kmp.project.schedule.entity

import kmp.project.schedule.viewModel.RepeatMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleEntity(
    @SerialName("uuid")
    val uuid: String,
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
    val sequence: Int,
    @SerialName("isFinished")
    val finished: Boolean,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("device")
    val device: String
)

@Serializable
data class ScheduleResult(
    @SerialName("success")
    val success: Int,
    @SerialName("failure")
    val failure: Int
)