package kmp.project.schedule.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.navigation.navDisplay.HomeNavDisplay
import kmp.project.schedule.ui.composableItem.CalendarPager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.ui.composableItem.ConfirmDialog
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.util.timeUtil.getCurrentDate
import kmp.project.schedule.util.timeUtil.getMonthDateRange
import kmp.project.schedule.util.viewUtil.ReorderHapticFeedbackType
import kmp.project.schedule.util.viewUtil.rememberReorderHapticFeedback
import kmp.project.schedule.viewModel.AuthViewModel
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 主页
 * @param isCompact 是否是竖屏模式
 * @param navController 导航控制器
 * @param scheduleViewModel 日程数据的 ViewModel
 * @param homePageStateViewModel 主页状态的 ViewModel
 * @param authViewModel 认证相关的 ViewModel
 * @param date 当前显示的日期
 * @param coroutineScope 协程作用域
 * @param snackbarHostState Snackbar 状态
 * @param backStack 导航回退栈
 */
@Composable
fun MainPage(
    isCompact: Boolean,
    navController: NavHostController = rememberNavController(),
    scheduleViewModel: ScheduleViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    authViewModel: AuthViewModel,
    date: MutableState<LocalDate>,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    backStack: SnapshotStateList<Any>
) {
    val scheduleList = remember { scheduleViewModel.schedules }

    LaunchedEffect("${date.value.year}-${date.value.month.number}-${date.value.day}", scheduleViewModel.monthSchedules.size) {
        scheduleViewModel.loadMonthSchedulesToCache(
            authViewModel.getUserName()?:"",
            getMonthDateRange(date.value).first,
            getMonthDateRange(date.value).second
        )
        scheduleViewModel.loadTodaySchedulesToCache(date)
    }
        HomeNavDisplay(
            backStack = backStack,
            modifier = Modifier,
            isCompact = isCompact,
            scheduleList = scheduleList,
            date = date,
            scheduleViewModel = scheduleViewModel,
            authViewModel = authViewModel,
            homePageStateViewModel = homePageStateViewModel,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
//    }
}

/**
 * 竖屏模式下的主页， 横屏模式下的日程信息页
 * 列表状体通过[LazyListState]同步，用于横竖屏切换时保持滚动位置
 * @param isCompact 是否是竖屏模式
 * @param modifier 修饰符
 * @param list 日程列表
 * @param date 当前显示的日期
 * @param onScheduleCardClick 点击日程卡片的回调
 * @param onAddClick 点击添加按钮的回调
 * @param showSnackBar 显示 Snackbar 的回调
 * @param nickname 用户昵称
 * @param username 用户名
 */
@Suppress("UnrememberedMutableState", "FrequentlyChangingValue")
@Composable
fun ScheduleInformation(
    scheduleViewModel: ScheduleViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    list: List<Schedule>,
    date: MutableState<LocalDate>,
    onScheduleCardClick: (Schedule) -> Unit,
    onAddClick: () -> Unit,
    showSnackBar: (String) -> Unit,
    nickname: String,
    username: String
) {
    val haptic = rememberReorderHapticFeedback()
    val showEditMode by homePageStateViewModel.showEditMode.collectAsState()
    val showConfirmDialog by homePageStateViewModel.showConfirmDialog.collectAsState()
    val showDatePickerDialog by homePageStateViewModel.showDatePickerDialog.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            homePageStateViewModel.savedFirstVisibleIndex,
            homePageStateViewModel.savedScrollOffset
        )
    }

    // 监听滚动状态变化，保存到 ViewModel
    LaunchedEffect(listState) {
        snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }.collect { (index, offset) ->
                homePageStateViewModel.savedFirstVisibleIndex = index
                homePageStateViewModel.savedScrollOffset = offset
            }
    }

    val reorderableLazyColumnState = rememberReorderableLazyListState(listState) { from, to ->
        scheduleViewModel.schedules = scheduleViewModel.schedules.apply {
            if (isCompact) {
                add(to.index - 1, removeAt(from.index - 1))
            } else {
                add(to.index, removeAt(from.index))
            }
        }

        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    EditModeBackHandler(
        showDeleteTopDocker = showEditMode,
        closeEditMode = { homePageStateViewModel.setShowEditMode(false) }
    )

    if (!showEditMode) {
        scheduleViewModel.schedulesToDelete.clear()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            AnimatedVisibility(
                visible = showEditMode && !isCompact,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TopBar(
                    date = date,
                    homePageStateViewModel = homePageStateViewModel,
                    onAddClick = onAddClick,
                    onCloseClick = {
                        homePageStateViewModel.setShowEditMode(false)
                        scheduleViewModel.schedulesToDelete.clear()
                    },
                    onDeleteClick = {
                        if (scheduleViewModel.schedulesToDelete.isNotEmpty())
                            homePageStateViewModel.setShowConfirmDialog(true)
                    },
                    onMoveClick = {},
                    viewModel = scheduleViewModel,
                    isCompact = isCompact,
                    nickname = nickname
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
                            visible = true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TopBar(
                                date = date,
                                homePageStateViewModel = homePageStateViewModel,
                                onAddClick = onAddClick,
                                onCloseClick = {
                                    homePageStateViewModel.setShowEditMode(false)
                                    scheduleViewModel.schedulesToDelete.clear()
                                },
                                onDeleteClick = {
                                    if (scheduleViewModel.schedulesToDelete.isNotEmpty())
                                        homePageStateViewModel.setShowConfirmDialog(true)
                                },
                                onMoveClick = {},
                                viewModel = scheduleViewModel,
                                isCompact = isCompact,
                                nickname = nickname
                            )
                        }
                    }
                }

                itemsIndexed(items = list, key = { _, schedule -> schedule.uuid }) { _, schedule ->
                    ReorderableItem(
                        reorderableLazyColumnState,
                        key = schedule.uuid,
                        animateItemModifier = Modifier.animateItem(
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = 650f,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        )
                    ) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                        val isSelected = mutableStateOf(
                            scheduleViewModel.schedulesToDelete.contains(schedule.uuid)
                        )
                        ScheduleCard(
                            modifier = Modifier
                                .zIndex(elevation.value),
                            schedule = schedule,
                            isSelected = isSelected.value,
                            onCardClick = { schedule ->
                                if (showEditMode && !isSelected.value) {
                                    scheduleViewModel.schedulesToDelete.add(schedule.uuid)
                                } else if (showEditMode && isSelected.value) {
                                    scheduleViewModel.schedulesToDelete.remove(schedule.uuid)
                                } else {
                                    onScheduleCardClick(schedule)
                                }
                            },
                            onCardLongClick = {
                                if (scheduleViewModel.schedulesToDelete.isEmpty())
                                    scheduleViewModel.schedulesToDelete.add(it)
                                homePageStateViewModel.setShowEditMode(true)
                            },
                            scope = this,
                            haptic = haptic,
                            onDragStopped = { scheduleViewModel.reorderSchedules(showSnackBar) },
                            onScheduleFinished = { scheduleViewModel.finishSchedule(it, showSnackBar) }
                        )
                    }
                }
            }
        }

        if (isCompact) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.TopCenter),
                visible = listState.firstVisibleItemIndex >= 1,
            ) {
                FloatingActionBar(
                    homePageStateViewModel = homePageStateViewModel,
                    onAddClick = onAddClick,
                    onCloseClick = { homePageStateViewModel.setShowEditMode(false) },
                    onMoveClick = {},
                    onDeleteClick = {
                        if (scheduleViewModel.schedulesToDelete.isNotEmpty())
                            homePageStateViewModel.setShowConfirmDialog(true)
                    },
                    onCalendarPickerClick = { homePageStateViewModel.setShowDatePickerDialog(!showDatePickerDialog) }
                )
            }
        }

        if (showConfirmDialog) {
            ConfirmDialog(
                title = "删除日程",
                content = "已选择${scheduleViewModel.schedulesToDelete.size}条日程\n" +
                        "将同步删除本地和云端的日程，且删除后不可恢复\n确定要删除这些日程吗？",
                onConfirm = {
                    homePageStateViewModel.setShowConfirmDialog(false)
                    scheduleViewModel.deleteSchedules(username, showSnackBar)
                },
                onDismiss = { homePageStateViewModel.setShowConfirmDialog(false) }
            )
        }

        if (showDatePickerDialog) {
            CalendarPickerDialog(
                onDismiss = { homePageStateViewModel.setShowDatePickerDialog(false) },
                onDateSelected = { date.value = it },
                date = date,
                scheduleViewModel = scheduleViewModel
            )
        }
    }
}

/**
 * 横屏模式下在屏幕右侧显示的其他信息
 */
@Composable
fun OtherInformation(
    modifier: Modifier,
    date: MutableState<LocalDate>,
    scheduleCount: Int,
    onAddClick: () -> Unit,
    scheduleViewModel: ScheduleViewModel
) {
    LazyColumn(
        modifier = modifier
            .statusBarsPadding()
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
                    text = "今日剩余${scheduleCount}项日程",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onAddClick,
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
            CalendarPager(
                currentDate = date.value,
                onDayClick = { date.value = it },
                onMonthChanged = { date.value = it },
                viewMode = 1,
                scheduleViewModel = scheduleViewModel
            )
        }
    }
}

/**
 * 顶部用户栏
 *
 * 显示用户信息
 */
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    date: MutableState<LocalDate>,
    homePageStateViewModel: HomePageStateViewModel,
    onAddClick: () -> Unit,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveClick: () -> Unit,
    viewModel: ScheduleViewModel,
    isCompact: Boolean,
    nickname: String
) {
    val showEditMode by homePageStateViewModel.showEditMode.collectAsState()
    val showDatePickerDialog by homePageStateViewModel.showDatePickerDialog.collectAsState()

    val animateColor by animateColorAsState(
        if (showEditMode)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.background
    )

    val animationSpec = spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = 0.65f,
        visibilityThreshold = IntSize.VisibilityThreshold
    )

    Column (
        modifier = modifier.background(animateColor),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 60.dp,
                bottom = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = showEditMode,
                enter = fadeIn() + expandHorizontally(animationSpec),
            ) {
                IconButton(
                    onClick = onCloseClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            if (isCompact) {
                userImage("TEST URL", 30.dp)
            }

            AnimatedVisibility(
                visible = !showEditMode,
            ) {
                Text(
                    text = nickname,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            AnimatedVisibility(
                visible = showEditMode && !isCompact
            ) {
                Text(
                    text = "已选择${viewModel.schedulesToDelete.size}项日程",
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCompact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { homePageStateViewModel.setShowDatePickerDialog(!showDatePickerDialog) },
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

            AnimatedVisibility(
                visible = showEditMode,
                enter = fadeIn() + expandHorizontally(animationSpec),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onMoveClick,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Move"
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }

        //顶栏标题
        AnimatedContent(targetState = showEditMode && isCompact) {
            if (it) {
                Text(
                    text = "已选择${viewModel.schedulesToDelete.size}项日程",
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                )
            } else if (isCompact) {
                Text(
                    text = getCurrentDate(date.value),
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingActionBar(
    homePageStateViewModel: HomePageStateViewModel,
    onAddClick: () -> Unit,
    onCloseClick: () -> Unit,
    onCalendarPickerClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveClick: () -> Unit
) {
    val showEditMode by homePageStateViewModel.showEditMode.collectAsState()
    val animationSpec = spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = 0.65f,
        visibilityThreshold = IntSize.VisibilityThreshold
    )

    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .statusBarsPadding()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCalendarPickerClick
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date"
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
            AnimatedVisibility(
                visible = showEditMode,
                enter = fadeIn() + expandHorizontally(animationSpec),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMoveClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Move"
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.inversePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }
        }
    }
}

@Composable
expect fun EditModeBackHandler(showDeleteTopDocker: Boolean, closeEditMode: () -> Unit)