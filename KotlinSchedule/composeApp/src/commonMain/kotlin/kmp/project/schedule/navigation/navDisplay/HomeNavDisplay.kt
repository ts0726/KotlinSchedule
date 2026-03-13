package kmp.project.schedule.navigation.navDisplay

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.navigation.key.CreateSchedule
import kmp.project.schedule.navigation.key.ScheduleDetail
import kmp.project.schedule.navigation.key.ScheduleList
import kmp.project.schedule.ui.home.NewSchedule
import kmp.project.schedule.ui.home.OtherInformation
import kmp.project.schedule.ui.home.ScheduleDetail
import kmp.project.schedule.ui.home.ScheduleInformation
import kmp.project.schedule.util.viewUtil.showSnackBar
import kmp.project.schedule.viewModel.AuthViewModel
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeNavDisplay(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier,
    isCompact: Boolean,
    scheduleList: List<Schedule>,
    date: MutableState<LocalDate>,
    scheduleViewModel: ScheduleViewModel,
    authViewModel: AuthViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    key(isCompact) {
        val listDetailStrategy = rememberListDetailSceneStrategy<Any>()
        NavDisplay(
            sceneStrategy = listDetailStrategy,
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = { key ->
                when (key) {
                    is ScheduleList -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = {
                                OtherInformation(
                                    modifier = modifier,
                                    date = date,
                                    scheduleCount = scheduleList.filter { !it.finished.toBoolean() }.size,
                                    onAddClick = {
                                        if (backStack.last() is CreateSchedule) {
                                            return@OtherInformation
                                        }
                                        backStack.add(CreateSchedule)
                                    },
                                    scheduleViewModel = scheduleViewModel
                                )
                            }
                        )
                    ) {
                        ScheduleInformation(
                            scheduleViewModel = scheduleViewModel,
                            homePageStateViewModel = homePageStateViewModel,
                            isCompact = isCompact,
                            modifier = modifier,
                            list = scheduleViewModel.schedules,
                            date = date,
                            onScheduleCardClick = {schedule ->
                                if (backStack.last() is ScheduleDetail)
                                    backStack.removeLastOrNull()
                                backStack.add(
                                    ScheduleDetail(
                                        schedule
                                    )
                                )
                            },
                            onAddClick = {
                                if (backStack.last() is CreateSchedule) {
                                    return@ScheduleInformation
                                }
                                backStack.add(CreateSchedule)
                            },
                            showSnackBar = { showSnackBar(snackbarHostState, coroutineScope, it) },
                            nickname = authViewModel.getNickname() ?: "游客",
                            username = authViewModel.getUserName() ?: ""
                        )
                    }

                    is ScheduleDetail -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        ScheduleDetail(
                            uuid = key.schedule.uuid,
                            viewModel = scheduleViewModel,
                            showSnackBar = { showSnackBar(snackbarHostState, coroutineScope, it) },
                            onBack = {
                                scheduleViewModel.reset()
                                backStack.removeLastOrNull()
                            },
                            onEdit = {
                                if (backStack.last() is CreateSchedule) {
                                    return@ScheduleDetail
                                }
                                backStack.add(CreateSchedule)
                            },
                            isCompact = isCompact
                        )
                    }

                    is CreateSchedule -> NavEntry(
                        key = key,
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        scheduleViewModel.date.value = date.value
                        NewSchedule(
                            onBack = {
                                scheduleViewModel.reset()
                                backStack.removeLastOrNull()
                            },
                            onSave = {
                                if (scheduleViewModel.title.value.isEmpty()) {
                                    scheduleViewModel.title.value = "未命名事件"
                                }
                                if (scheduleViewModel.content.value.isEmpty()) {
                                    scheduleViewModel.content.value = "无备注"
                                }
                                scheduleViewModel.userName.value = authViewModel.getUserName() ?: ""
                                coroutineScope.launch {
                                    scheduleViewModel.onSave(
                                        date.value,
                                        showSnackBar = { showSnackBar(snackbarHostState, coroutineScope, it) }
                                    )
                                    withContext(Dispatchers.Main) {
                                        backStack.removeLastOrNull()
                                    }
                                }
                            },
                            viewModel = scheduleViewModel,
                            isCompact = isCompact,
                            date = date
                        )
                    }

                    else -> error("Unknown key: $key")
                }
            },
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
        )
    }
}