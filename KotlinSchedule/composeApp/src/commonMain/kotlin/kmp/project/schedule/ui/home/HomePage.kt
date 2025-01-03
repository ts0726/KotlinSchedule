package kmp.project.schedule.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.model.ScheduleViewModel
import kmp.project.schedule.navigation.HomeNavHost
import kmp.project.schedule.ui.composableItem.CalendarPager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.util.getCurrentDate
import kotlinx.datetime.LocalDate

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
    scheduleViewModel: ScheduleViewModel,
    date: MutableState<LocalDate>,
) {
    val scheduleList = remember { scheduleViewModel.schedules }

    LaunchedEffect(date.value) {
        scheduleViewModel.loadSchedules(sdk, date)
    }

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
                scheduleList,
                date,
                onScheduleCardClick = { uuid ->
                    navController.navigate("scheduleDetail/$uuid") {
                        //清除栈中的日程详情页面，防止叠加
                        popUpTo("scheduleDetail/{uuid}") {
                            inclusive = true
                        }
                    }
                },
                onAddClick = { navController.navigate("home_add") }
            )
        }
        //竖屏模式下显示日程信息，横屏模式下作为操作页显示其他信息
        HomeNavHost(
            sdk = sdk,
            navController = navController,
            modifier = Modifier.weight(1f),
            isCompact = isCompact,
            listState = listState,
            scheduleList = scheduleList,
            date = date,
//            viewModel = viewModel,
            scheduleViewModel = scheduleViewModel
        )
    }
}

/**
 * 竖屏模式下的主页， 横屏模式下的日程信息页
 * 列表状体通过[LazyListState]同步，用于横竖屏切换时保持滚动位置
 * @param isCompact 是否是竖屏模式
 * @param modifier 修饰符
 * @param listState 列表状态
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun scheduledInformation(
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    list: List<Schedule>,
    date: MutableState<LocalDate>,
    onScheduleCardClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val showDeleteTopDocker = remember { mutableStateOf(false) }
    val topDeleteDockerHeight = remember { mutableStateOf(0) }

    BackHandler( showDeleteTopDocker = showDeleteTopDocker )

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            AnimatedVisibility(
                visible = showDeleteTopDocker.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        topDeleteDockerHeight.value = coordinates.size.height
                    }
            ) {
                topDeleteDocker(
                    modifier = Modifier,
                    onAddClick = onAddClick
                )
            }

            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxHeight(),
            ) {
                if (isCompact) {
                    item(key = "topDocker") {
                        AnimatedVisibility(
                            visible = !showDeleteTopDocker.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            topDocker(
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 60.dp,
                                    bottom = 10.dp
                                ),
                                date = date,
                                onAddClick = onAddClick
                            )
                        }
                    }
                }

                items(items = list, key = { it.uuid }) {
                    scheduleCard(
                        modifier = if (isCompact) Modifier else Modifier.animateItemPlacement(),
                        it,
                        onCardClick = { uuid ->
                            onScheduleCardClick(uuid)
                        },
                        onCardLongClick = { showDeleteTopDocker.value = !showDeleteTopDocker.value }
                    )
                }
            }
        }

        if (isCompact) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = listState.firstVisibleItemIndex >= 1 && !showDeleteTopDocker.value,
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    }
}

/**
 * 横屏模式下在屏幕右侧显示的其他信息
 */
@Composable
fun otherInformation(
    modifier: Modifier,
    navController: NavHostController,
    date: MutableState<LocalDate>
) {
    LazyColumn(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxHeight()
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
            CalendarPager(date.value) { date.value = it }
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
    modifier: Modifier,
    date: MutableState<LocalDate>,
    onAddClick: () -> Unit
) {
    val showDatePickerDialog = remember { mutableStateOf(false) }

    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //显示当前日期
            Text(
                text = getCurrentDate(date.value),
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

            IconButton(
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
    if (showDatePickerDialog.value) {
        CalendarPickerDialog(
            onDismiss = { showDatePickerDialog.value = false },
            onDateSelected = {date.value = it},
            date = date
        )
    }
}

/**
 * 顶部删除栏
 */
@Composable
fun topDeleteDocker(
    modifier: Modifier,
    bottomPadding: Dp = 30.dp,
    onAddClick: () -> Unit
) {
    Column (
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = modifier.padding(top = bottomPadding, bottom = bottomPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //显示当前日期
            Text(
                text = "已选择0项日程",
                fontWeight = FontWeight.W800,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {  },
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Repeat"
                )
            }

            IconButton(
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
}

@Composable
expect fun BackHandler(showDeleteTopDocker: MutableState<Boolean>)