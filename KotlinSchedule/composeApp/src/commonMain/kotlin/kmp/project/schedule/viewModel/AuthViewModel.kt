package kmp.project.schedule.viewModel

import kmp.project.schedule.ScheduleSDK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.entity.RegisterEntity
import kmp.project.schedule.entity.AuthEntity
import kmp.project.schedule.entity.NicknameRequest
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.authApi
import kmp.project.schedule.util.SettingsName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val sdk: ScheduleSDK): ViewModel() {
    private val _tokenState = MutableStateFlow<ApiResult<AuthEntity>?>(null)
    val tokenState: StateFlow<ApiResult<AuthEntity>?> = _tokenState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<Unit>?>(null)
    val registerState: StateFlow<ApiResult<Unit>?> = _registerState.asStateFlow()

    private val _nicknameState = MutableStateFlow(
        sdk.getSetting(SettingsName.NICKNAME.toString()) ?: "未登录"
    )
    val nicknameState: StateFlow<String> = _nicknameState.asStateFlow()

    fun login(loginEntity: LoginEntity) {
        viewModelScope.launch {
            val result = authApi.login(loginEntity)
            _tokenState.value = result
            if (result is ApiResult.Success) {
                val nickname = sdk.getSetting(SettingsName.NICKNAME.toString()) ?: "未登录"
                _nicknameState.value = nickname
            }
        }
    }

    fun register(registerEntity: RegisterEntity) {
        viewModelScope.launch {
            val result = authApi.register(registerEntity)
            _registerState.value = result
        }
    }

    fun refresh(refreshToken: String) {
        viewModelScope.launch {
            val result = authApi.refresh(refreshToken)
            _tokenState.value = result
        }
    }

    /**
     * update nickname from network
     */
    fun updateNickname(
        nicknameRequest: NicknameRequest,
        token: String,
        showSnackBar: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authApi.updateNickname(nicknameRequest, token)
            if (result is ApiResult.Success) {
                sdk.addSetting(SettingsName.NICKNAME.toString(), nicknameRequest.nickname)
                _nicknameState.value = nicknameRequest.nickname
                showSnackBar("昵称修改成功")
            } else if (result is ApiResult.Error) {
                showSnackBar("昵称修改失败：${result.message}")
            }
        }
    }


    fun resetTokenState() {
        _tokenState.value = null
    }

    fun resetNickname() {
        val nickname = sdk.getSetting(SettingsName.NICKNAME.toString()) ?: "未登录"
        _nicknameState.value = nickname
    }

    fun getAccessToken(): String? {
        return sdk.getSetting(SettingsName.ACCESS_TOKEN.toString())
    }

    fun getRefreshToken(): String? {
        return sdk.getSetting(SettingsName.REFRESH_TOKEN.toString())
    }

    fun updateTokens(accessToken: String, refreshToken: String) {
        sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), accessToken)
        sdk.addSetting(SettingsName.REFRESH_TOKEN.toString(), refreshToken)
    }

    fun clearTokens() {
        sdk.removeSetting(SettingsName.ACCESS_TOKEN.toString())
        sdk.removeSetting(SettingsName.REFRESH_TOKEN.toString())
    }

    fun getNickname(): String? {
        return sdk.getSetting(SettingsName.NICKNAME.toString())
    }

    /**
     * update settings nickname
     */
    fun updateNickname(nickname: String) {
        sdk.addSetting(SettingsName.NICKNAME.toString(), nickname)
    }

    fun clearNickname() {
        sdk.removeSetting(SettingsName.NICKNAME.toString())
    }
}