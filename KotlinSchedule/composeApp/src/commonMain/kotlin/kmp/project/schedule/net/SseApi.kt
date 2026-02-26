package kmp.project.schedule.net

import androidx.compose.runtime.MutableState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.deserialize
import io.ktor.client.plugins.sse.sse
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.entity.SchedulesToDelete
import kmp.project.schedule.entity.SseHello
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.EOFException

class SseApi(
    private val sseClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun receiveEvent(
        scheduleViewModel: ScheduleViewModel,
        currentDate: MutableState<LocalDate>,
        onSessionIdReceived: (() -> Unit)? = null
    ) {
        try {
            sseClient.sse(urlString = "$baseUrl/sse/events", deserialize = { typeInfo, jsonString ->
                val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                Json.decodeFromString(serializer, jsonString)!!
            }) {
                println("JSON input: $this")
                incoming.collect { event ->
                    if (event.data?.isNotEmpty()!!) {
                        try {
                            scheduleViewModel.addScheduleFromSseServer(
                                deserialize<ScheduleEntity>(event.data)!!,
                                currentDate.value
                            )
                        } catch (_: Exception) {
                            try {
                                ApiConfig.sessionId = deserialize<SseHello>(event.data)!!.sessionId
                                onSessionIdReceived?.invoke()
                            } catch (_: Exception) {
                                val deleteList = deserialize<SchedulesToDelete>(event.data)?.uuids
                                scheduleViewModel.deleteSchedulesFromSSEServer(deleteList!!)
                            }
                        }
                    }
                }
            }
        } catch (e: SSEClientException) {
            println("SSEClientException: ${e.message}")
            delay(1000L)
        } catch (e: EOFException) {
            println("EOFException: ${e.message}")
            delay(1000L)
        }
    }
}