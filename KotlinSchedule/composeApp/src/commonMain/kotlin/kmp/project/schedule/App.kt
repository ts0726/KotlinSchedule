package kmp.project.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import kmp.project.schedule.navigation.key.MyPage
import kmp.project.schedule.navigation.key.ScheduleList
import kmp.project.schedule.net.SseConnectionStatus
import kmp.project.schedule.net.sseApi
import kmp.project.schedule.ui.composableItem.SseConnectionStatusIndicator
import kmp.project.schedule.ui.home.MainPage
import kmp.project.schedule.ui.my.MyPage
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.viewModel.AuthViewModel
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.koinInject
import kotlin.time.Clock

@Composable
expect fun PlatformKoinApplication(content: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    PlatformKoinApplication {
        val windowSizeInfo = currentWindowAdaptiveInfo()
        val isCompact = !windowSizeInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
        val scheduleViewModel: ScheduleViewModel = koinInject()
        val authViewModel: AuthViewModel = koinInject()
        val homePageStateViewModel: HomePageStateViewModel = koinInject()
        val coroutineScope = rememberCoroutineScope()
        val pageID = remember { mutableIntStateOf(0) }
        val date = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
        val snackbarHostState = remember { SnackbarHostState() }
        val homeBackStack = remember { mutableStateListOf<Any>(ScheduleList) }
        val myBackStack = remember { mutableStateListOf<Any>(MyPage) }

        LaunchedEffect(homePageStateViewModel.retryState.value) {
            sseApi.receiveEvent(homePageStateViewModel, scheduleViewModel, date) {
                val currentUser = authViewModel.getUserName()
                if (!currentUser.isNullOrEmpty()) {
                    // 启动时增量同步数据
                    scheduleViewModel.syncDataIncrementally(
                        userName = currentUser,
                        showSnackBar = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        },
                        currentDate = date.value
                    )
                }
            }
        }

        PlatformTheme {
            CustomScaffold(
                scheduleViewModel = scheduleViewModel,
                authViewModel = authViewModel,
                homePageStateViewModel = homePageStateViewModel,
                isCompact = isCompact,
                snackbarHostState = snackbarHostState,
                coroutineScope = coroutineScope,
                pageID = pageID,
                date = date,
                homeBackStack = homeBackStack,
                myBackStack = myBackStack
            )
        }
    }
}

/**
 * 应用界面框架
 *
 * 使用[CustomScaffold]作为主页的容器，通过检测屏幕宽度来决定是否显示侧边栏
 *
 * @param isCompact 检测显示设备屏幕宽度, 用于判断是否显示侧边栏
 */
@Composable
fun CustomScaffold(
    scheduleViewModel: ScheduleViewModel,
    authViewModel: AuthViewModel,
    homePageStateViewModel: HomePageStateViewModel,
    isCompact: Boolean,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    pageID: MutableIntState,
    date: MutableState<LocalDate>,
    homeBackStack: SnapshotStateList<Any>,
    myBackStack: SnapshotStateList<Any>
) {
    Row (
        Modifier
            .fillMaxSize()
    ) {
        if (!isCompact) {
            SideNavRail(
                pageID = pageID,
                status = homePageStateViewModel.connectionStatus.value,
                onIndicatorClick = {
                    homePageStateViewModel.retryState.value = !homePageStateViewModel.retryState.value
                }
            )
        }
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxWidth(),
            bottomBar = {
                if (isCompact) {
                    BottomNavBar(pageID)
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            content = { innerPadding ->
                ContentContainer(
                    content = {
                        when (pageID.intValue) {
                            0 -> MainPage(
                                isCompact = isCompact,
                                scheduleViewModel = scheduleViewModel,
                                authViewModel = authViewModel,
                                homePageStateViewModel = homePageStateViewModel,
                                date = date,
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState,
                                backStack = homeBackStack
                            )
                            1 -> MyPage(
                                authViewModel = authViewModel,
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope,
                                backStack = myBackStack
                            )
                        }
                    },
                    padding = innerPadding
                )
            }
        )
    }
}

/**
 * 页面显示容器
 *
 * 边距和对齐方式已设置好，只需通过此组件中调用页面即可
 */
@Composable
fun ContentContainer(
    content: @Composable () -> Unit,
    padding: PaddingValues
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

/**
 * 应用侧栏
 *
 * 通过[NavigationRail]实现侧边导航栏，当屏幕宽度过大或设备处于横屏时，显示该组件
 *
 */
@Composable
fun SideNavRail(
    pageID: MutableIntState,
    status: SseConnectionStatus,
    onIndicatorClick: () -> Unit
) {
    val cardSizes = remember { mutableStateListOf(50.dp, 30.dp, 30.dp) }
    val items = listOf("主页", "我的")

    //根据pageID初始化图标状态
    for (i in 0 .. 2) {
        cardSizes[i] = if (i == pageID.intValue) 50.dp else 30.dp
    }

    NavigationRail (
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
        ) {
            userImage("testURL", 40.dp)
        }

        items.forEachIndexed { index, tag ->
            val cardSize = animateDpAsState(targetValue = cardSizes[index]).value
            Column (
                modifier = Modifier
                    .clickable(
                        onClick = {
                            pageID.intValue = index
                            for (i in 0..2) {
                                cardSizes[i] = if (i == index) 50.dp else 30.dp
                            }
                        },
                        indication = null,
                        interactionSource = MutableInteractionSource()
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .size(cardSize)
                        .padding(top = 5.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationIcon(index)
                    }
                }
                AnimatedVisibility(visible = index == pageID.intValue) {
                    Text(
                        text = tag,
                        modifier = Modifier
                            .padding(bottom = 5.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        SseConnectionStatusIndicator(
            connectionStatus = status,
            onClick = onIndicatorClick
        )
    }
}

/**
 * 应用底部导航栏
 *
 * 当显示应用的设备为竖屏或屏幕狭窄时，显示底部导航栏
 */
@Composable
fun BottomNavBar(pageID: MutableIntState) {
    val items = listOf("主页", "我的")
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEachIndexed { index, _ ->
            NavigationBarItem(
                icon = { NavigationIcon(index) },
                selected = index == pageID.intValue,
                onClick = {
                    pageID.intValue = index
                },
            )
        }
    }
}

/**
 * 导航栏图标
 */
@Composable
fun NavigationIcon(
    index: Int,
) {
    when (index) {
        0 -> Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.padding(3.dp))
        else -> Icon(Icons.Filled.Person, contentDescription = "Person", modifier = Modifier.padding(3.dp))
    }
}

@Composable
expect fun PlatformTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)