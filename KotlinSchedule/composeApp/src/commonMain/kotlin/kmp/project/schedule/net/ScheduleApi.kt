package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kmp.project.schedule.entity.ScheduleEntity
import kotlinx.serialization.json.Json

object ScheduleApi {
    private const val BASE_URL = "http://127.0.0.1:8080/schedules"

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
    }

    suspend fun getAllSchedules(accessToken: String): List<ScheduleEntity> {
        return client.get(BASE_URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${accessToken}")
            }
        }.body()
    }

}