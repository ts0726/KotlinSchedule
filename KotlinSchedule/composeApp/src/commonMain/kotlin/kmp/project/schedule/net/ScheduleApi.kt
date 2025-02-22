package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.entity.ScheduleResult
import java.net.ConnectException

class ScheduleApi(
    private val clientWithToken: HttpClient,
    private val baseUrl: String
) {
    suspend fun addSchedule(scheduleEntity: ScheduleEntity): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/create") {
                contentType(ContentType.Application.Json)
                setBody(scheduleEntity)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未经授权的访问或token已过期")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun deleteSchedule(uuid: String): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.delete("$baseUrl/schedules/$uuid")
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未经授权的访问或token已过期")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun deleteSchedules(uuids: List<String>): ApiResult<ScheduleResult> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/delete") {
                contentType(ContentType.Application.Json)
                setBody(uuids)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未经授权的访问或token已过期")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    private suspend fun <T> executeRequest(
        block: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        return try {
            block()
        } catch (e: ConnectException) {
            ApiResult.Error(NetStatus.NETWORK_ERROR, "网络异常")
        }
    }
}