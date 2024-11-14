package kmp.project.schedule.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.data.ScheduleData
import kmp.project.schedule.model.NewScheduleViewModel
import kmp.project.schedule.ui.composableItem.CalendarPaager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.util.getCurrentDate
import kmp.project.schedule.util.getDayTimestamp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun mainPagePreview() {
    topDocker(0.dp)
}

/**
 * 主页
 * @param isCompact 是否是竖屏模式
 * @param navController 导航控制器
 * @param listState 列表状态
 */
@Composable
fun mainPage(
    sdk: ScheduleSDK,
    isCompact: Boolean,
    navController: NavHostController = rememberNavController(),
    listState: LazyListState,
    viewModel: NewScheduleViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        //横屏模式下显示日程信息，通过listState同步竖屏时的滚动位置
        if (!isCompact) {
            scheduledInformation(
                isCompact,
                Modifier.weight(1f),
                listState,
                navController
            )
        }
        //竖屏模式下显示日程信息，横屏模式下作为操作页显示其他信息
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.weight(1f)
        ) {
            composable("home") {
                if (isCompact) {
                    scheduledInformation(
                        isCompact,
                        Modifier.weight(1f),
                        listState,
                        navController
                    )
                } else {
                    otherInformation(
                        Modifier.weight(1f),
                        navController
                    )
                }

            }
            composable("home_add") {
                NewSchedule(
                    onBack = { navController.popBackStack() },
                    onSave = { viewModel.onSave(sdk, navController) },
                    viewModel = viewModel
                )
            }
            composable("scheduleDetail/{title}/{content}") { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val content = backStackEntry.arguments?.getString("content") ?: ""
                ScheduleDetail(ScheduleData(title, content), navController, viewModel)
            }
        }
    }
}

/**
 * 竖屏模式下的主页， 横屏模式下的日程信息页
 * 列表状体通过[LazyListState]同步，用于横竖屏切换时保持滚动位置
 * @param isCompact 是否是竖屏模式
 * @param modifier 修饰符
 * @param listState 列表状态
 */
@Composable
fun scheduledInformation(
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    navController: NavHostController
) {
    //测试数据
    val list = arrayListOf<String>()
    repeat(100) {
        list.add("Item $it")
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxHeight()
                .padding(start = 10.dp, end = 10.dp),
        ) {
            if (isCompact) {
                item {
                    topDocker()
                }
            }
            items(list.size) { index ->
                scheduleCard("日程$index", list[index], navController)
            }
        }

        if (isCompact) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("home_add")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun otherInformation(
    modifier: Modifier,
    navController: NavHostController
) {
    rememberDatePickerState(
        initialSelectedDateMillis = getDayTimestamp()
    )
    LazyColumn(
        modifier = modifier.fillMaxHeight()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp),
            ) {
                Text(
                    text = "宜看窗外云卷云舒~",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate("home_add") },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 20.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
        }

        item {
            CalendarPaager {}
        }
    }
}

/**
 * 顶部用户栏
 *
 * 显示用户信息
 */
@Composable
fun topDocker(
    bottomPadding: Dp = 30.dp
) {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    Column (
        modifier = Modifier
            .padding(10.dp, 30.dp, 10.dp, bottomPadding),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "今天是",
            fontWeight = FontWeight.W800,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //显示当前日期
            Text(
                text = getCurrentDate(),
                fontWeight = FontWeight.W800,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { showDatePickerDialog.value = !showDatePickerDialog.value },
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Repeat"
                )
            }
        }
    }
    if (showDatePickerDialog.value) {
        CalendarPickerDialog(
            onDismiss = { showDatePickerDialog.value = false },
            onDateSelected = {}
        )
    }
}

