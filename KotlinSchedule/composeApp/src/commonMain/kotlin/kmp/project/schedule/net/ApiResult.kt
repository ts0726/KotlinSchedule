package kmp.project.schedule.net

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val status: NetStatus, val message: String? = null) : ApiResult<Nothing>()
}

enum class NetStatus {
    INVALID_USER,
    NETWORK_ERROR,
    UNAUTHORIZED,
    SERVER_ERROR,
    UNKNOWN_ERROR,
    CREATED
}