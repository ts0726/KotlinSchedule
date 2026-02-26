package kmp.project.schedule.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kmp.project.schedule.entity.AuthEntity
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.entity.NicknameRequest
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.authApi
import kmp.project.schedule.util.SettingsManager
import kmp.project.schedule.util.SettingsName
import kmp.project.schedule.util.tokenUtil.AuthTokenManager
import kmp.project.schedule.util.tokenUtil.getUsernameFromToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val settingsManager: SettingsManager
): ViewModel() {
    private val tokenManager = AuthTokenManager(settingsManager)

    private val _authState = MutableStateFlow<ApiResult<AuthEntity>?>(null)
    val authState: StateFlow<ApiResult<AuthEntity>?> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<Unit>?>(null)
    val registerState: StateFlow<ApiResult<Unit>?> = _registerState.asStateFlow()

//    private val _refreshTokenState = MutableStateFlow<ApiResult<RefreshTokenEntity>?>(null)
//    val refreshTokenState: StateFlow<ApiResult<RefreshTokenEntity>?> = _refreshTokenState.asStateFlow()

    private val _nicknameState by lazy {
        MutableStateFlow(
            settingsManager.getSetting(SettingsName.NICKNAME.toString(), String::class.java) ?: "未登录"
        )
    }
    val nicknameState: StateFlow<String> get() = _nicknameState.asStateFlow()

    fun login(
        loginEntity: LoginEntity,
        requestFinished: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authApi.login(loginEntity)
            _authState.value = result
            if (result is ApiResult.Success) {
                val nickname = settingsManager.getSetting(SettingsName.NICKNAME.toString(), String::class.java) ?: "未登录"
                _nicknameState.value = nickname
            }
            requestFinished()
        }
    }

//    fun register(registerEntity: RegisterEntity) {
//        viewModelScope.launch {
//            val result = authApi.register(registerEntity)
//            _registerState.value = result
//        }
//    }

//    fun refresh(refreshToken: String) {
//        viewModelScope.launch {
//            val result = authApi.refresh(refreshToken)
//            _refreshTokenState.value = result
//        }
//    }

    /**
     * update nickname from network
     */
    fun updateNickname(
        nicknameRequest: NicknameRequest,
        showSnackBar: (String) -> Unit,
        requestFinished: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authApi.updateNickname(nicknameRequest)
            requestFinished()
            if (result is ApiResult.Success) {
                settingsManager.addSetting(SettingsName.NICKNAME.toString(), nicknameRequest.nickname)
                _nicknameState.value = nicknameRequest.nickname
                showSnackBar("昵称修改成功")
            } else if (result is ApiResult.Error) {
                showSnackBar("昵称修改失败：${result.message}")
            }
        }
    }


    fun resetTokenState() {
        _authState.value = null
    }

    fun resetNickname() {
        val nickname = settingsManager.getSetting(SettingsName.NICKNAME.toString(), String::class.java) ?: "未登录"
        _nicknameState.value = nickname
    }

    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }

    fun updateTokens(accessToken: String, refreshToken: String) {
        tokenManager.addToken(accessToken, refreshToken)
    }

    fun clearTokens() {
        tokenManager.removeToken()
    }

    fun getNickname(): String? {
        return settingsManager.getSetting(SettingsName.NICKNAME.toString(), String::class.java)
    }

    /**
     * update settings nickname
     */
    fun updateNickname(nickname: String) {
        settingsManager.addSetting(SettingsName.NICKNAME.toString(), nickname)
    }

    fun clearNickname() {
        settingsManager.removeSetting(SettingsName.NICKNAME.toString())
    }

    fun getUserName(): String? {
        return getUsernameFromToken(settingsManager.getSetting(SettingsName.REFRESH_TOKEN.toString(), String::class.java)?:"")
    }
}