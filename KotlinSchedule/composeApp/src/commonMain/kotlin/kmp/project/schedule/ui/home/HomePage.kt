package kmp.project.schedule.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.model.ScheduleViewModel
import kmp.project.schedule.navigation.HomeNavHost
import kmp.project.schedule.ui.composableItem.CalendarPager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.ui.composableItem.ConfirmDialog
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.util.getCurrentDate
import kmp.project.schedule.util.rememberReorderHapticFeedback
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
    date: MutableState<LocalDate>,
    coroutineScope: CoroutineScope
) {
    val scheduleList = remember { scheduleViewModel.schedules }

    LaunchedEffect(date.value) {
        scheduleViewModel.loadSchedules(date)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        //横屏模式下显示日程信息，通过listState同步竖屏时的滚动位置
        if (!isCompact) {
            scheduledInformation(
                scheduleViewModel,
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
            navController = navController,
            modifier = Modifier.weight(1f),
            isCompact = isCompact,
            listState = listState,
            scheduleList = scheduleList,
            date = date,
            scheduleViewModel = scheduleViewModel,
            coroutineScope = coroutineScope
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
    viewModel: ScheduleViewModel,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    list: List<Schedule>,
    date: MutableState<LocalDate>,
    onScheduleCardClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val haptic = rememberReorderHapticFeedback()
    val showEditMode = remember { mutableStateOf(false) }
    val topDeleteDockerHeight = remember { mutableStateOf(0) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    val reorderableLazyColumnState = rememberReorderableLazyListState(listState) { from, to ->
        viewModel.schedules = viewModel.schedules.apply {
            if (isCompact) {
                add(to.index - 1, removeAt(from.index - 1))
            } else {
                add(to.index, removeAt(from.index))
            }
        }
    }

    BackHandler( showDeleteTopDocker = showEditMode )

    if (!showEditMode.value) {
        viewModel.schedulesToDelete.clear()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            AnimatedVisibility(
                visible = showEditMode.value && !isCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        topDeleteDockerHeight.value = coordinates.size.height
                    }
            ) {
                topBar(
                    date = date,
                    onAddClick = onAddClick,
                    onCloseClick = {
                        showEditMode.value = false
                        viewModel.schedulesToDelete.clear()
                    },
                    onDeleteClick = {
                        if (viewModel.schedulesToDelete.isNotEmpty())
                            showConfirmDialog.value = true
                    },
                    onMoveClick = {},
                    showEditMode = showEditMode,
                    viewModel = viewModel,
                    isCompact = isCompact
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
//                            visible = !showDeleteTopDocker.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            topBar(
                                date = date,
                                onAddClick = onAddClick,
                                onCloseClick = {
                                    showEditMode.value = false
                                    viewModel.schedulesToDelete.clear()
                                },
                                onDeleteClick = {
                                    if (viewModel.schedulesToDelete.isNotEmpty())
                                        showConfirmDialog.value = true
                                },
                                onMoveClick = {},
                                showEditMode = showEditMode,
                                viewModel = viewModel,
                                isCompact = isCompact
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
                            viewModel.schedulesToDelete.contains(schedule.uuid)
                        )
                        scheduleCard(
                            modifier = Modifier
                                .zIndex(elevation.value),
                            schedule = schedule,
                            isSelected = isSelected.value,
                            onCardClick = { uuid ->
                                if (showEditMode.value && !isSelected.value) {
                                    viewModel.schedulesToDelete.add(uuid)
                                } else if (showEditMode.value && isSelected.value) {
                                    viewModel.schedulesToDelete.remove(uuid)
                                } else {
                                    onScheduleCardClick(uuid)
                                }
                            },
                            onCardLongClick = {
                                if (viewModel.schedulesToDelete.size == 0)
                                    viewModel.schedulesToDelete.add(it)
                                showEditMode.value = true
                            },
                            scope = this,
                            haptic = haptic,
                            onDragStopped = { viewModel.reorderSchedules() }
                        )
                    }
                }
            }
        }

        if (isCompact) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomEnd),
                visible = listState.firstVisibleItemIndex >= 1,
            ) {
                FloatingActionButton(
                    onClick = {
                        if (viewModel.schedulesToDelete.isNotEmpty()) {
                            showConfirmDialog.value = true
                        } else {
                            onAddClick()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {
                    AnimatedContent(targetState = showEditMode.value) {
                        if (it) {
                            Row {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                                )
                                Text(
                                    text = "已选${viewModel.schedulesToDelete.size}项",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 5.dp, end = 10.dp)
                                )
                            }
                        } else {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }

        if (showConfirmDialog.value) {
            ConfirmDialog(
                title = "删除日程",
                content = "已选择${viewModel.schedulesToDelete.size}条日程\n" +
                        "将同步删除本地和云端的日程，且删除后不可恢复\n确定要删除这些日程吗？",
                onConfirm = {
                    showConfirmDialog.value = false
                    viewModel.deleteSchedules()
                    viewModel.schedulesToDelete.clear()
                },
                onDismiss = { showConfirmDialog.value = false }
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
    showEditMode: MutableState<Boolean>,
    onAddClick: () -> Unit,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveClick: () -> Unit,
    viewModel: ScheduleViewModel,
    isCompact: Boolean
) {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    val animateColor by animateColorAsState(
        if (showEditMode.value)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.background
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
                visible = showEditMode.value
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
                visible = !showEditMode.value
            ) {
                Text(
                    text = "离线模式",
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

            AnimatedVisibility(
                visible = showEditMode.value
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
        AnimatedContent(targetState = showEditMode.value) {
            if (it) {
                Text(
                    text = "已选择${viewModel.schedulesToDelete.size}项日程",
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                )
            } else {
                Text(
                    text =  getCurrentDate(date.value),
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
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

@Composable
expect fun BackHandler(showDeleteTopDocker: MutableState<Boolean>)

//class ReorderHandler(val viewModel: ScheduleViewModel): ReorderHapticFeedback() {
//    override fun performHapticFeedback(type: ReorderHapticFeedbackType) {
//        when (type) {
//            ReorderHapticFeedbackType.END -> {
//                viewModel.updateSequence()
//            }
//            ReorderHapticFeedbackType.START -> {}
//            ReorderHapticFeedbackType.MOVE -> {}
//        }
//    }
//}