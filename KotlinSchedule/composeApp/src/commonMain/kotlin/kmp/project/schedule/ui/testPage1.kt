package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.schedule.net.ApiConfig
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.sdk.ScheduleSDK
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.util.SettingsName
import kmp.project.schedule.util.timeUtil.LunarUtil
import kmp.project.schedule.viewModel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn


@Suppress("CoroutineCreationDuringComposition")
@Composable
fun TestPage1(
    onButtonClick: () -> Unit,
    sdk: ScheduleSDK
) {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    val time = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    val today = time.value.atStartOfDayIn(TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
    val lunar = LunarUtil(today).getChineseLunarDay()
    val receivedData = remember { mutableStateOf("") }
    val token = remember { mutableStateOf("") }
    val authViewModel = AuthViewModel(sdk)

//    loadingDialog(
//        title = "正在登录",
//        content = "等待响应中",
//        onDismiss = {}
//    )

    authViewModel.authState
        .onEach { result ->
            when(result) {
                is ApiResult.Success -> {
                    println("success")
                    sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), result.data.accessToken)
                }
                is ApiResult.Error -> {
                    sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), result.status.toString())
                }
                null -> {

                }
            }
        }
        .launchIn(CoroutineScope(Dispatchers.IO))
    authViewModel.registerState
        .onEach { result ->
            when(result) {
                is ApiResult.Success -> {
                    println("注册成功")
                }
                is ApiResult.Error -> {
                    sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), result.message!!)
                }
                null -> {

                }
            }
        }
        .launchIn(CoroutineScope(Dispatchers.IO))
    Column {
//        Text(text = convertLocalDateToDate(time.value))
        Text(text = "农历${lunar}")
        IconButton(
            onClick = { showDatePickerDialog.value = !showDatePickerDialog.value },
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Repeat"
            )
        }
        if (showDatePickerDialog.value) {
            CalendarPickerDialog(
                onDismiss = { showDatePickerDialog.value = false },
                onDateSelected = {day ->
                    time.value = day
                },
                date = time
            )
        }
        Button(onClick = onButtonClick) {
            Text("test")
        }

        Button(onClick = {
            println("test")
//            authViewModel.login(LoginEntity("test", "tt111"))
//            authViewModel.refresh("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsIm5hbWUiOiJ0ZXN0IiwiZXhwIjoxNzM5NTg4NTYzfQ.nXRV-bEkFiEBx6r7ZVKRjV5qw26b2W6WIhSS9Cp3eug")
//                .launchIn(Dispatchers.IO)
        }) {
            Text("发送测试")
        }

        Button(
            onClick = {
                sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), "test token")
            }
        ) {
            Text("save token")
        }

        Button(
            onClick = {
//                token.value = sdk.getSetting(SettingsName.ACCESS_TOKEN.toString(), String::class.java).toString()
            }
        ) {
            Text("get token")
        }

        Text(text = receivedData.value)

        Text(text = token.value)

        ApiConfig.sessionId?.let { Text(text = it) }
    }
}