package kmp.project.schedule.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kmp.project.schedule.net.SseConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomePageStateViewModel : ViewModel() {
    private val _showEditMode = MutableStateFlow(false)
    val showEditMode: StateFlow<Boolean> = _showEditMode

//    private val _topDeleteDockerHeight = MutableStateFlow(0)

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog: StateFlow<Boolean> = _showConfirmDialog

    private val _showDatePickerDialog = MutableStateFlow(false)
    val showDatePickerDialog: StateFlow<Boolean> = _showDatePickerDialog

    // 保存滚动状态
    var savedFirstVisibleIndex: Int = 0

    var savedScrollOffset: Int = 0

    val connectionStatus = mutableStateOf(SseConnectionStatus.CLOSED)

    val retryState = mutableStateOf(false)

    fun setShowEditMode(value: Boolean) {
        _showEditMode.value = value
    }

    fun setShowConfirmDialog(value: Boolean) {
        _showConfirmDialog.value = value
    }

    fun setShowDatePickerDialog(value: Boolean) {
        _showDatePickerDialog.value = value
    }
}