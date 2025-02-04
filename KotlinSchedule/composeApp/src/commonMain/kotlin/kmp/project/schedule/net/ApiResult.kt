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

fun NetStatus.toResultString() = when (this) {
    NetStatus.INVALID_USER -> "用户名或密码错误"
    NetStatus.NETWORK_ERROR -> "网络异常，请检查网络配置"
    NetStatus.UNAUTHORIZED -> "未授权的访问"
    NetStatus.SERVER_ERROR -> "服务器内部错误，请联系管理员"
    NetStatus.UNKNOWN_ERROR -> "未知错误"
    NetStatus.CREATED -> "已创建"
}