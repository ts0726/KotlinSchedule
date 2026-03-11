package kmp.project.schedule.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.domain.sync.SyncStatus
import kmp.project.schedule.entity.RepeatMode
import kmp.project.schedule.ui.composableItem.ConfirmDialog
import kmp.project.schedule.util.getRepeat
import kmp.project.schedule.util.timeUtil.convertLocalDateToDate
import kmp.project.schedule.util.timeUtil.convertLocalDateToDateSimple
import kmp.project.schedule.util.timeUtil.convertTimestampToLocalDate
import kmp.project.schedule.util.timeUtil.getDaysFromToday
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.datetime.LocalDate

/**
 * 日程详情页
 * @param uuid 日程UUID
 * @param viewModel 日程ViewModel
 * @param showSnackBar 显示SnackBar的函数，参数为要显示的消息
 * @param onBack 返回函数
 * @param onEdit 编辑函数
 */
@Composable
fun ScheduleDetail(
    uuid: String,
    viewModel: ScheduleViewModel,
    showSnackBar: (String) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    isCompact: Boolean
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
                deleteSchedule = { uuid, username ->
                    viewModel.deleteSchedule(
                        uuid = uuid,
                        userName = username,
                        showSnackBar = showSnackBar
                    )
                },
                viewModel,
                schedule!!,
                onBack,
                onEdit
            )
            ScheduleDetailContent(schedule!!, isCompact)
        }
    } else {
        onBack()
    }
}

/**
 * 日程详情页顶部栏,用于返回和呼出菜单
 * @param deleteSchedule 删除日程的函数，参数为日程UUID和用户名
 * @param viewModel 日程ViewModel
 * @param schedule 日程数据
 * @param onBack 返回函数
 * @param onEdit 编辑函数
 */
@Composable
fun ScheduleDetailTopBar(
    deleteSchedule: (String, String) -> Unit,
    viewModel: ScheduleViewModel,
    schedule: Schedule,
    onBack: () -> Unit,
    onEdit: () -> Unit
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
            onClick = onBack,
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
                        viewModel.id.intValue = schedule.id.toInt()
                        viewModel.uuid.value = schedule.uuid
                        viewModel.date.value = LocalDate.fromEpochDays(schedule.date.toInt())
                        viewModel.title.value = schedule.title
                        viewModel.content.value = schedule.content!!
                        viewModel.location.value = schedule.location!!
                        viewModel.repeatMode.value = RepeatMode.valueOf(schedule.repeatMode)
                        viewModel.sequence.intValue = schedule.sequence.toInt()
                        onEdit()
//                        navHostController.navigate("home_add")
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
                deleteSchedule(schedule.uuid, schedule.username)
//                navHostController.navigateUp()'
                onBack()
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
    schedule: Schedule,
    isCompact: Boolean
) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 10.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = schedule.title,
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 50.sp
                )
                SyncStatus(
                    syncStatus = schedule.sync_status,
                    modifier = Modifier.padding(top = 15.dp)
                )
            }

            if (!isCompact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GeneralBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 10.dp, top = 10.dp, bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        icon = Icons.Filled.DateRange,
                        contentDescription = "Date",
                        title = "日期",
                        mainText = convertLocalDateToDate(LocalDate.fromEpochDays(schedule.date.toInt())),
                        subText = getDaysStringFromToday(LocalDate.fromEpochDays(schedule.date.toInt()))
                    )
                    GeneralBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(top = 10.dp, bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        icon = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        title = "地点",
                        mainText = schedule.location ?: "未知地点",
                    )
                }
            } else {
                GeneralBox(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(top = 10.dp, bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    icon = Icons.Filled.DateRange,
                    contentDescription = "Date",
                    title = "日期",
                    mainText = convertLocalDateToDate(LocalDate.fromEpochDays(schedule.date.toInt())),
                    subText = getDaysStringFromToday(LocalDate.fromEpochDays(schedule.date.toInt()))
                )
                GeneralBox(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(top = 10.dp, bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    icon = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    title = "地点",
                    mainText = schedule.location ?: "未知地点",
                )
            }

            InformationBox(
                schedule,
                modifier = Modifier.padding(top = 10.dp)
            )

            GeneralBox(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                icon = Icons.AutoMirrored.Filled.Notes,
                contentDescription = "Notes",
                title = "备注",
                mainText = schedule.content ?: "无备注"
            )
        }
    }
}

@Composable
private fun InformationBox(
    schedule: Schedule,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Information",
                modifier = Modifier.padding(end = 10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "更多信息",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeneralBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 10.dp, top = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                icon = Icons.Filled.AccountCircle,
                contentDescription = "Account",
                title = "用户",
                mainText = if (schedule.username == "") "游客" else schedule.username
            )
            GeneralBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                icon = Icons.Filled.MailOutline,
                contentDescription = "Device",
                title = "设备",
                mainText = "来自" + schedule.device
            )
        }
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeneralBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 10.dp, top = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                icon = Icons.Filled.EventRepeat,
                contentDescription = "Repeat",
                title = "重复模式",
                mainText = getRepeat(
                    LocalDate.fromEpochDays(schedule.date.toInt()),
                    RepeatMode.valueOf(schedule.repeatMode)
                )
            )
            GeneralBox(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                icon = Icons.Filled.EditCalendar,
                contentDescription = "Last",
                title = "上次修改",
                mainText = convertLocalDateToDateSimple(
                    convertTimestampToLocalDate(schedule.timestamp)
                )
            )
        }
    }
}

@Composable
private fun GeneralBox (
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    title: String,
    mainText: String,
    subText: String = "",
) {
    Box(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp, top = 20.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.padding(end = 10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    fontSize = 20.sp
                )
            }
            Text(
                text = mainText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    start = 10.dp,
                    bottom = if (subText.isNotEmpty()) 10.dp else 20.dp,
                    top = 10.dp
                ),
                lineHeight = 25.sp
            )
            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(start = 10.dp, bottom = 20.dp)
                )
            }
        }
    }
}

fun getDaysStringFromToday(date: LocalDate): String {
    val days = getDaysFromToday(date).toInt()
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

@Composable
private fun SyncStatus(
    syncStatus: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(getSyncStatusColor(syncStatus))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getSyncStatusIcon(syncStatus),
            contentDescription = "Sync Status",
            tint = getSyncStatusTextColor(syncStatus),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = getSyncStatusString(syncStatus),
            color = getSyncStatusTextColor(syncStatus),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

fun getSyncStatusString(syncStatus: String): String {
    return when(syncStatus) {
        SyncStatus.SYNCED.toString() -> "已同步"
        SyncStatus.PENDING.toString() -> "待同步"
        SyncStatus.FAILED.toString() -> "同步失败"
        else -> "未知状态"
    }
}

@Composable
private fun getSyncStatusColor(syncStatus: String): Color {
    return when(syncStatus) {
        SyncStatus.SYNCED.toString() -> MaterialTheme.colorScheme.primary // 绿色
        SyncStatus.PENDING.toString() -> Color(0xFFFFC107) // 黄色
        SyncStatus.FAILED.toString() -> MaterialTheme.colorScheme.error // 红色
        else -> Color.Gray
    }
}

@Composable
private fun getSyncStatusIcon(syncStatus: String): ImageVector {
    return when(syncStatus) {
        SyncStatus.SYNCED.toString() -> Icons.Filled.Done
        SyncStatus.PENDING.toString() -> Icons.Filled.Refresh
        SyncStatus.FAILED.toString() -> Icons.Filled.Warning
        else -> Icons.Filled.Info
    }
}

@Composable
private fun getSyncStatusTextColor(syncStatus: String): Color {
    return when(syncStatus) {
        SyncStatus.SYNCED.toString() -> MaterialTheme.colorScheme.onPrimary
        SyncStatus.PENDING.toString() -> Color(0xFF795548)
        SyncStatus.FAILED.toString() -> MaterialTheme.colorScheme.onError
        else -> Color.Gray
    }
}