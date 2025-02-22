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
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import kmp.project.schedule.navigation.HomeNavHost
import kmp.project.schedule.ui.composableItem.CalendarPager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.ui.composableItem.ConfirmDialog
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.util.viewUtil.ReorderHapticFeedbackType
import kmp.project.schedule.util.timeUtil.getCurrentDate
import kmp.project.schedule.util.viewUtil.rememberReorderHapticFeedback
import kmp.project.schedule.viewModel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDate
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 主页
 * @param isCompact 是否是竖屏模式
 * @param navController 导航控制器
 * @param listState 列表状态
 */
@Composable
fun mainPage(
    isCompact: Boolean,
    navController: NavHostController = rememberNavController(),
    listState: LazyListState,
    scheduleViewModel: ScheduleViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    authViewModel: AuthViewModel,
    date: MutableState<LocalDate>,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val scheduleList = remember { scheduleViewModel.schedules }

    LaunchedEffect(date.value) {
        scheduleViewModel.loadSchedules(authViewModel.getUserName()?:"", date)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        //横屏模式下显示日程信息，通过listState同步竖屏时的滚动位置
        if (!isCompact) {
            scheduledInformation(
                scheduleViewModel = scheduleViewModel,
                homePageStateViewModel = homePageStateViewModel,
                isCompact = isCompact,
                modifier = Modifier.weight(1f),
                listState = listState,
                list = scheduleList,
                date = date,
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
            navController = navController,
            modifier = Modifier.weight(1f),
            isCompact = isCompact,
            listState = listState,
            scheduleList = scheduleList,
            date = date,
            scheduleViewModel = scheduleViewModel,
            authViewModel = authViewModel,
            homePageStateViewModel = homePageStateViewModel,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState
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
@Composable
fun scheduledInformation(
    scheduleViewModel: ScheduleViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    list: List<Schedule>,
    date: MutableState<LocalDate>,
    onScheduleCardClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val haptic = rememberReorderHapticFeedback()
    val showEditMode by homePageStateViewModel.showEditMode.collectAsState()
    val showConfirmDialog by homePageStateViewModel.showConfirmDialog.collectAsState()
    val showDatePickerDialog by homePageStateViewModel.showDatePickerDialog.collectAsState()
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
//                    .onGloballyPositioned { coordinates ->
//                        homePageStateViewModel.setTopDeleteDockerHeight(coordinates.size.height)
//                    }
            ) {
                topBar(
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
//                    showEditMode = showEditMode,
                    viewModel = scheduleViewModel,
                    isCompact = isCompact,
//                    showDatePickerDialog = showDatePickerDialog
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
                            topBar(
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
//                                showEditMode = showEditMode,
                                viewModel = scheduleViewModel,
                                isCompact = isCompact,
//                                showDatePickerDialog = showDatePickerDialog
                            )
                        }
                    }
                }

                itemsIndexed(items = list, key = { _, schedule -> schedule.uuid }) {index, schedule ->
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
                        scheduleCard(
                            modifier = Modifier
                                .zIndex(elevation.value),
                            schedule = schedule,
                            isSelected = isSelected.value,
                            onCardClick = { uuid ->
                                if (showEditMode && !isSelected.value) {
                                    scheduleViewModel.schedulesToDelete.add(uuid)
                                } else if (showEditMode && isSelected.value) {
                                    scheduleViewModel.schedulesToDelete.remove(uuid)
                                } else {
                                    onScheduleCardClick(uuid)
                                }
                            },
                            onCardLongClick = {
                                if (scheduleViewModel.schedulesToDelete.isEmpty())
                                    scheduleViewModel.schedulesToDelete.add(it)
                                homePageStateViewModel.setShowEditMode(true)
                            },
                            scope = this,
                            haptic = haptic,
                            onDragStopped = { scheduleViewModel.reorderSchedules() }
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
                floatingActionBar(
//                    showEditMode = showEditMode,
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
                    scheduleViewModel.deleteSchedules()
                    scheduleViewModel.schedulesToDelete.clear()
                },
                onDismiss = { homePageStateViewModel.setShowConfirmDialog(false) }
            )
        }

        if (showDatePickerDialog) {
            CalendarPickerDialog(
                onDismiss = { homePageStateViewModel.setShowDatePickerDialog(false) },
                onDateSelected = { date.value = it },
                date = date
            )
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
    date: MutableState<LocalDate>,
    scheduleCount: Int
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
                    text = "今日剩余${scheduleCount}项日程",
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
fun topBar(
    modifier: Modifier = Modifier,
    date: MutableState<LocalDate>,
    homePageStateViewModel: HomePageStateViewModel,
    onAddClick: () -> Unit,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveClick: () -> Unit,
    viewModel: ScheduleViewModel,
    isCompact: Boolean
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
                    text = "离线模式",
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
fun floatingActionBar(
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