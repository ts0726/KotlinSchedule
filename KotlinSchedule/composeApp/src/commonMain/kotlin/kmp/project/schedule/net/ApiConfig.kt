package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kmp.project.schedule.entity.RefreshTokenEntity
import kmp.project.schedule.util.tokenUtil.AuthTokenManager
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform
import kotlin.time.Duration.Companion.seconds

// 全局配置
object ApiConfig {
    const val BASE_URL = "http://192.168.0.9:8080"
    var sessionId: String? = null
    val httpClientWithoutToken: HttpClient = HttpClient{
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    val httpClientWithToken: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(Auth) {
            bearer {
                val tokenManager = AuthTokenManager(
                    settingsManager = KoinPlatform.getKoin().get()
                )
                loadTokens {
                    BearerTokens(
                        accessToken = tokenManager.getAccessToken()?:"",
                        refreshToken = tokenManager.getRefreshToken()
                    )
                }
                if (tokenManager.getAccessToken() != null) {
                    refreshTokens {
                        val response: RefreshTokenEntity = httpClientWithoutToken.post("$BASE_URL/refresh") {
                            markAsRefreshTokenRequest()
                            headers { append(HttpHeaders.Authorization, "Bearer ${oldTokens?.refreshToken}") }
                            contentType(ContentType.Application.Json)
                        }.body()
                        tokenManager.addToken(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken
                        )
                        BearerTokens(
                            response.accessToken,
                            response.refreshToken
                        )
                    }
                }

            }
        }
        install(DefaultRequest) {
            if (!sessionId.isNullOrBlank()) {
                println("sessionId: $sessionId")
                header("X-Session-Id", sessionId)
            }
        }
    }
    val sseClient: HttpClient = httpClientWithToken.config {
        install(SSE) {
            maxReconnectionAttempts = 5
            reconnectionTime = 5.seconds
            showRetryEvents()
            showCommentEvents()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL // 启用详细日志
        }
    }
}


val authApi = AuthApi(
    clientWithoutToken = ApiConfig.httpClientWithoutToken,
    clientWithToken = ApiConfig.httpClientWithToken,
    baseUrl = ApiConfig.BASE_URL
)

val scheduleApi = ScheduleApi(
    clientWithToken = ApiConfig.httpClientWithToken,
    baseUrl = ApiConfig.BASE_URL
)

val sseApi = SseApi (
    sseClient = ApiConfig.sseClient,
    baseUrl = ApiConfig.BASE_URL
)