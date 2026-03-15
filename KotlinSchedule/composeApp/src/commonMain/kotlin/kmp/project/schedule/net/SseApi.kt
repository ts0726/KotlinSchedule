package kmp.project.schedule.net

import androidx.compose.runtime.MutableState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.deserialize
import io.ktor.client.plugins.sse.sse
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.entity.SchedulesToDelete
import kmp.project.schedule.entity.SseHello
import kmp.project.schedule.viewModel.HomePageStateViewModel
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
        homePageStateViewModel: HomePageStateViewModel,
        scheduleViewModel: ScheduleViewModel,
        currentDate: MutableState<LocalDate>,
        onSessionIdReceived: (() -> Unit)? = null
    ) {
        var retryCount = 0
        val maxRetries = 5 // 或者设置一个合理的上限，配合 isActive 控制

        while (retryCount < maxRetries) {
            homePageStateViewModel.connectionStatus.value = SseConnectionStatus.CONNECTING
            try {
                println("SSE: trying to connect... (count: ${retryCount + 1})")

                sseClient.sse(urlString = "$baseUrl/sse/events", deserialize = { typeInfo, jsonString ->
                    val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                    Json.decodeFromString(serializer, jsonString)!!
                }) {
                    incoming.collect { event ->
                        // 重置重试计数，因为成功接收到了数据
                        homePageStateViewModel.connectionStatus.value = SseConnectionStatus.CONNECTED
                        retryCount = 0

                        if (event.data?.isNotEmpty() == true) {
                            try {
                                scheduleViewModel.addScheduleFromSseServer(
                                    deserialize<ScheduleEntity>(event.data)!!,
                                    currentDate.value
                                )
                            } catch (_: Exception) {
                                try {
                                    val hello = deserialize<SseHello>(event.data)
                                    ApiConfig.sessionId = hello!!.sessionId
                                    println("SSE: received Session ID: ${ApiConfig.sessionId}")
                                    onSessionIdReceived?.invoke()
                                } catch (_: Exception) {
                                    try {
                                        val deleteList = deserialize<SchedulesToDelete>(event.data)?.uuids
                                        if (!deleteList.isNullOrEmpty()) {
                                            scheduleViewModel.deleteSchedulesFromSSEServer(deleteList)
                                        }
                                    } catch (e: Exception) {
                                        println("SSE: deserialize delete list failed: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                }
                // 如果 sse 块正常退出（非异常），说明服务器关闭了连接，也需要重连
                println("SSE: connection closed, prepare to reconnect...")

            } catch (e: SSEClientException) {
                e.printStackTrace()
            } catch (e: EOFException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            retryCount++
            delay(2000L)
        }

        homePageStateViewModel.connectionStatus.value = SseConnectionStatus.CLOSED
    }
}