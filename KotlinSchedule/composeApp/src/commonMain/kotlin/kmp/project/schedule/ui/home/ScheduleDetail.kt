package kmp.project.schedule.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kmp.project.schedule.data.ScheduleData
import kmp.project.schedule.model.NewScheduleViewModel
import kmp.project.schedule.util.convertLocalDateToDate
import kmp.project.schedule.util.getDaysFromToday
import kmp.project.schedule.util.getRepeat
import kotlinx.datetime.LocalDate

/**
 * 日程详情页
 * @param scheduleData 日程数据
 * @param navHostController 导航控制器
 * @param viewModel 日程ViewModel
 */
@Composable
fun ScheduleDetail(
    scheduleData: ScheduleData,
    navHostController: NavHostController,
    viewModel: NewScheduleViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScheduleDetailTopBar(
            navHostController,
            viewModel,
            scheduleData
        )
        ScheduleDetailContent(scheduleData = scheduleData)
    }
}

/**
 * 日程详情页顶部栏,用于返回和呼出菜单
 * @param navHostController 导航控制器
 * @param viewModel 日程ViewModel
 * @param scheduleData 日程数据
 */
@Composable
fun ScheduleDetailTopBar(
    navHostController: NavHostController,
    viewModel: NewScheduleViewModel,
    scheduleData: ScheduleData
) {
    val expanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { navHostController.navigateUp() },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
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
                        viewModel.date.value = scheduleData.date
                        viewModel.title.value = scheduleData.title
                        viewModel.content.value = scheduleData.content
                        viewModel.repeatMode.value = scheduleData.repeatMode
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
                    onClick = {},
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
}

/**
 * 日程详情页内容
 * @param scheduleData 日程数据
 */
@Composable
fun ScheduleDetailContent(
    scheduleData: ScheduleData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = scheduleData.title,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp, top = 10.dp)
        )
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
                text = convertLocalDateToDate(scheduleData.date),
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
                text = getDaysStringFromToday(scheduleData.date),
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
                text = getRepeat(scheduleData.date, scheduleData.repeatMode),
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
                text = "未设置地点",
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
                modifier = Modifier.padding(end = 15.dp)
            )
            Text(
                text = scheduleData.content,
                fontSize = 20.sp
            )
        }
    }
}

fun getDaysStringFromToday(date: LocalDate): String {
    val days = getDaysFromToday(date)
    if (days == 0) {
        return "今天"
    } else if (days == 1) {
        return "明天"
    } else if (days == -1) {
        return "昨天"
    } else if (days > 0){
        return "$days 天后"
    } else {
        return "${-days} 天前"
    }
}