package kmp.project.schedule.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.entity.RegisterEntity
import kmp.project.schedule.entity.TokenEntity
import kotlinx.serialization.json.Json

object AuthApi {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun login(loginEntity: LoginEntity): TokenEntity {
        return client.post("http://127.0.0.1:8080/auth/login") {
            setBody(loginEntity)
        }.body()
    }

    suspend fun register(registerEntity: RegisterEntity): Boolean {
        return client.post("http://127.0.0.1:8080/auth/register") {
            setBody(registerEntity)
        }.status.value == 201
    }

    suspend fun refresh(refreshToken: String): TokenEntity {
        return client.post("http://127.0.0.1:8080/refresh") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${refreshToken}")
            }
        }.body()
    }

}