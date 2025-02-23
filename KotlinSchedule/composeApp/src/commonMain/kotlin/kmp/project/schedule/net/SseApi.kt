package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.deserialize
import io.ktor.client.plugins.sse.sse
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okio.EOFException

class SseApi(
    private val sseClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun receiveEvent(scheduleViewModel: ScheduleViewModel) {
        try {
            sseClient.sse(urlString = "$baseUrl/sse/events", deserialize = { typeInfo, jsonString ->
                val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                Json.decodeFromString(serializer, jsonString)!!
            }) {
                incoming.collect { event ->
                    scheduleViewModel.addScheduleFromSseServer(deserialize<ScheduleEntity>(event.data)!!)
                    println("received event: ${deserialize<ScheduleEntity>(event.data)}")
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