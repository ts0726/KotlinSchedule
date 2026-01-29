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
import kmp.project.schedule.entity.SyncScheduleEntity
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
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以上传日程")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun deleteSchedule(uuid: String): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.delete("$baseUrl/schedules/$uuid")
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以删除日程")
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
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以删除日程")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun updateSchedule(scheduleEntity: ScheduleEntity): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/update") {
                contentType(ContentType.Application.Json)
                setBody(scheduleEntity)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以更新日程")
                404 -> ApiResult.Error(NetStatus.NOT_FOUND, "该日程未上传")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun updateSchedules(schedules: List<ScheduleEntity>): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/updates") {
                contentType(ContentType.Application.Json)
                setBody(schedules)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以更新日程")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun syncSchedules(): ApiResult<List<SyncScheduleEntity>> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/sync") {
                contentType(ContentType.Application.Json)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以同步日程")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun getSchedulesByUuids(uuids: List<String>): ApiResult<List<ScheduleEntity>> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/schedules/getByUuids") {
                contentType(ContentType.Application.Json)
                setBody(uuids)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "未登录或登陆已过期，请尝试重新登陆以获取日程")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    private suspend fun <T> executeRequest(
        block: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        return try {
            block()
        } catch (_: ConnectException) {
            ApiResult.Error(NetStatus.NETWORK_ERROR, "网络异常")
        }
    }
}