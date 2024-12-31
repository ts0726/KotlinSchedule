package kmp.project.schedule.navigation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.model.NewScheduleViewModel
import kmp.project.schedule.ui.home.NewSchedule
import kmp.project.schedule.ui.home.ScheduleDetail
import kmp.project.schedule.ui.home.otherInformation
import kmp.project.schedule.ui.home.scheduledInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * 主页导航
 *
 * @param sdk 数据库操作类
 * @param navController 导航控制器
 * @param modifier 修饰符
 * @param isCompact 是否是竖屏模式
 * @param listState 列表状态
 * @param scheduleList 日程列表
 * @param date 当前日期
 * @param viewModel 新建日程ViewModel
 */
@Composable
fun HomeNavHost(
    sdk: ScheduleSDK,
    navController: NavHostController,
    modifier: Modifier,
    isCompact: Boolean,
    listState: LazyListState,
    scheduleList: MutableState<List<Schedule>>,
    date: MutableState<LocalDate>,
    viewModel: NewScheduleViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            if (isCompact) {
                scheduledInformation(
                    isCompact,
                    modifier,
                    listState,
                    navController,
                    scheduleList.value,
                    date,
                    onScheduleCardClick = { uuid ->
                        navController.navigate("scheduleDetail/$uuid") {
                            //清除栈中的日程详情页面，防止叠加
                            popUpTo("scheduleDetail/{uuid}") {
                                inclusive = true
                            }
                        }
                    }
                )
            } else {
                otherInformation(
                    modifier,
                    navController,
                    date
                )
            }

        }
        composable("home_add") {
            NewSchedule(
                onBack = {
                    viewModel.reset()
                    navController.popBackStack()
                },
                onSave = {
                    if (viewModel.title.value.isEmpty()) {
                        viewModel.title.value = "未命名事件"
                    }
                    if (viewModel.content.value.isEmpty()) {
                        viewModel.content.value = "无"
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.onSave(sdk, navController)
                        scheduleList.value = sdk.getScheduleByDate(date.value.toEpochDays().toLong())
                        viewModel.reset()
                    }
                },
                viewModel = viewModel
            )
        }
        composable(
            route = "scheduleDetail/{uuid}",
            arguments = listOf(
                navArgument("uuid") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
        ) { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
            ScheduleDetail(sdk, uuid, navController, viewModel)
        }
    }
}