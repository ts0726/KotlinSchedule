package kmp.project.schedule.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.viewModel.ScheduleViewModel
import kmp.project.schedule.ui.composableItem.ConfirmDialog
import kmp.project.schedule.util.timeUtil.convertLocalDateToDate
import kmp.project.schedule.util.timeUtil.getDaysFromToday
import kmp.project.schedule.util.getRepeat
import kmp.project.schedule.viewModel.RepeatMode
import kotlinx.datetime.LocalDate

/**
 * 日程详情页
 * @param uuid 日程UUID
 * @param navHostController 导航控制器
 * @param viewModel 日程ViewModel
 */
@Composable
fun ScheduleDetail(
    uuid: String,
    navHostController: NavHostController,
    viewModel: ScheduleViewModel,
) {
    var schedule by remember { mutableStateOf<Schedule?>(null) }
    schedule = viewModel.loadScheduleByUUID(uuid)

    if (schedule != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ScheduleDetailTopBar(
                delateSchedule = { viewModel.deleteSchedule(it) },
                navHostController,
                viewModel,
                schedule!!
            )
            ScheduleDetailContent(schedule!!)
        }
    } else {
//        // 显示加载指示器或占位符
//        Text("Loading...")
        navHostController.popBackStack()
    }
}

/**
 * 日程详情页顶部栏,用于返回和呼出菜单
 * @param navHostController 导航控制器
 * @param viewModel 日程ViewModel
 * @param schedule 日程数据
 */
@Composable
fun ScheduleDetailTopBar(
    delateSchedule: (String) -> Unit,
    navHostController: NavHostController,
    viewModel: ScheduleViewModel,
    schedule: Schedule
) {
    val expanded = remember { mutableStateOf(false) }
    val showConfirmDialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { navHostController.navigateUp() },
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close"
            )
        }

        Text(
            text = "日程详情",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        IconButton(
            onClick = { expanded.value = true },
        ){
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu"
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded.value = false
                        viewModel.id.value = schedule.id.toInt()
                        viewModel.uuid.value = schedule.uuid
                        viewModel.date.value = LocalDate.fromEpochDays(schedule.date.toInt())
                        viewModel.title.value = schedule.title
                        viewModel.content.value = schedule.content!!
                        viewModel.location.value = schedule.location!!
                        viewModel.repeatMode.value = RepeatMode.valueOf(schedule.repeatMode)
                        viewModel.sequence.value = schedule.sequence.toInt()
                        navHostController.navigate("home_add")
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.padding(end = 5.dp)
                            )
                            Text("编辑")
                        }
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        expanded.value = false
                        showConfirmDialog.value = true
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.padding(end = 5.dp)
                            )
                            Text("删除")
                        }
                    }
                )
            }
        }
    }

    if (showConfirmDialog.value) {
        ConfirmDialog(
            title = "删除日程",
            content = "将同步删除本地和云端的日程，且删除后不可恢复\n确定要删除该日程吗？",
            onConfirm = {
                showConfirmDialog.value = false
                delateSchedule(schedule.uuid)
                navHostController.navigateUp()
            },
            onDismiss = { showConfirmDialog.value = false }
        )
    }
}

/**
 * 日程详情页内容
 * @param schedule 日程数据
 */
@Composable
fun ScheduleDetailContent(
    schedule: Schedule
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = schedule.title,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp, top = 10.dp),
            lineHeight = 45.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBox,
                contentDescription = "Account",
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = if (schedule.username == "") "游客" else schedule.username,
                fontSize = 20.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Date",
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = convertLocalDateToDate(LocalDate.fromEpochDays(schedule.date.toInt())),
                fontSize = 20.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "warning",
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = getDaysStringFromToday(LocalDate.fromEpochDays(schedule.date.toInt())),
                fontSize = 20.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Repeat",
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = getRepeat(
                    LocalDate.fromEpochDays(schedule.date.toInt()),
                    RepeatMode.valueOf(schedule.repeatMode)
                ),
                fontSize = 20.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "location",
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = schedule.location ?: "未知地点",
                fontSize = 20.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "content",
                modifier = Modifier.padding(end = 15.dp).align(Alignment.Top),
            )
            Text(
                text = schedule.content!!,
                fontSize = 20.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

fun getDaysStringFromToday(date: LocalDate): String {
    val days = getDaysFromToday(date)
    return if (days == 0) {
        "今天"
    } else if (days == 1) {
        "明天"
    } else if (days == -1) {
        "昨天"
    } else if (days > 0){
        "$days 天后"
    } else {
        "${-days} 天前"
    }
}