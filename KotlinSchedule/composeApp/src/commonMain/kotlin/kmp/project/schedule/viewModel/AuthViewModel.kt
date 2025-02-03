package kmp.project.schedule.viewModel

import kmp.project.schedule.ScheduleSDK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.entity.RegisterEntity
import kmp.project.schedule.entity.AuthEntity
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.authApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val sdk: ScheduleSDK): ViewModel() {
    private val _tokenState = MutableStateFlow<ApiResult<AuthEntity>?>(null)
    val tokenState: StateFlow<ApiResult<AuthEntity>?> = _tokenState.asStateFlow()

    private val _registerState = MutableStateFlow<ApiResult<Unit>?>(null)
    val registerState: StateFlow<ApiResult<Unit>?> = _registerState.asStateFlow()

    fun login(loginEntity: LoginEntity) {
        viewModelScope.launch {
            val result = authApi.login(loginEntity)
            _tokenState.value = result
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

    fun resetTokenState() {
        _tokenState.value = null
    }

}