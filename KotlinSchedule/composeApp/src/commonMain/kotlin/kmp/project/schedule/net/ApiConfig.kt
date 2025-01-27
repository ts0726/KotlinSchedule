package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// 全局配置
object ApiConfig {
    const val BASE_URL = "http://127.0.0.1:8080"
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
}

val authApi = AuthApi(ApiConfig.httpClient, ApiConfig.BASE_URL)