package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kmp.project.schedule.entity.AuthEntity
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.entity.NicknameRequest
import kmp.project.schedule.entity.RegisterEntity
import java.net.ConnectException

class AuthApi(
    private val clientWithoutToken: HttpClient,
    private val clientWithToken: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(loginEntity: LoginEntity): ApiResult<AuthEntity> {
        return executeRequest {
            val response = clientWithoutToken.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginEntity)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(response.body())
                401 -> ApiResult.Error(NetStatus.INVALID_USER, "用户名或密码错误")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

    suspend fun register(registerEntity: RegisterEntity): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithoutToken.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(registerEntity)
            }
            when (response.status.value) {
                200 -> ApiResult.Success(Unit)
                400 -> ApiResult.Error(NetStatus.CREATED, "用户已存在")
                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
            }
        }
    }

//    suspend fun refresh(refreshToken: String): ApiResult<RefreshTokenEntity> {
//        println("refresh")
//        return executeRequest {
//            val response = clientWithToken.post("$baseUrl/refresh") {
//                println("refresh token: Bearer $refreshToken")
//                headers { append(HttpHeaders.Authorization, "Bearer ${refreshToken}") }
//                println("headers: " + headers.get(HttpHeaders.Authorization))
//            }
//            when (response.status.value) {
//                200 -> ApiResult.Success(response.body())
//                401 -> ApiResult.Error(NetStatus.UNAUTHORIZED, "登录过期，请重新登陆")
//                else -> ApiResult.Error(NetStatus.SERVER_ERROR, "服务器错误")
//            }
//        }
//    }

    suspend fun updateNickname(nicknameRequest: NicknameRequest, token: String): ApiResult<Unit> {
        return executeRequest {
            val response = clientWithToken.post("$baseUrl/auth/updateNickname") {
                contentType(ContentType.Application.Json)
                setBody(nicknameRequest)
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
        } catch (e: Exception) {
            ApiResult.Error(NetStatus.UNKNOWN_ERROR, "未知错误: ${e.message}")
        }
    }
}