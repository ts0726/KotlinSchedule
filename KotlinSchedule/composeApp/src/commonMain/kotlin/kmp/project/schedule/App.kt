package kmp.project.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kmp.project.schedule.ui.TestPage1
import kmp.project.schedule.ui.home.mainPage
import kmp.project.schedule.ui.my.myPage
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.viewModel.AuthViewModel
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    val windowSize = calculateWindowSizeClass()
    val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    val sdk = getScheduleSDK()
    val scheduleViewModel: ScheduleViewModel = viewModel { ScheduleViewModel(sdk) }
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(sdk) }
    val homePageStateViewModel: HomePageStateViewModel = viewModel()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val pageID = remember { mutableIntStateOf(0) }
    val date = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }

    customTheme {
        CustomScaffold(
            scheduleViewModel = scheduleViewModel,
            authViewModel = authViewModel,
            homePageStateViewModel = homePageStateViewModel,
            isCompact = isCompact,
            listState = listState,
            coroutineScope = coroutineScope,
            pageID = pageID,
            date = date,
            sdk = sdk
        )
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
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    pageID: MutableIntState,
    date: MutableState<LocalDate>,
    sdk: ScheduleSDK
) {
    Row (
        Modifier
            .fillMaxSize()
    ) {
        if (!isCompact) {
            sideNavRail(pageID)
        }
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxWidth(),
            bottomBar = {
                if (isCompact) {
                    bottomNavBar(pageID)
                }
            },
            content = { innerPadding ->
                contentContainer(
                    content = {
                        when (pageID.value) {
                            0 -> mainPage(
                                isCompact = isCompact,
                                listState = listState,
                                scheduleViewModel = scheduleViewModel,
                                homePageStateViewModel = homePageStateViewModel,
                                date = date,
                                coroutineScope = coroutineScope
                            )
                            1 -> TestPage1(onButtonClick = {}, sdk = sdk)
                            2 -> myPage(sdk, authViewModel = authViewModel)
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
fun contentContainer(
    content: @Composable () -> Unit,
    padding: PaddingValues
) {
    Column(
        Modifier
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
fun sideNavRail(pageID: MutableIntState) {
    val cardSizes = remember { mutableStateListOf(50.dp, 30.dp, 30.dp) }
    val items = listOf("主页", "全部", "我的")

    //根据pageID初始化图标状态
    for (i in 0 .. 2) {
        cardSizes[i] = if (i == pageID.value) 50.dp else 30.dp
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

        items.forEachIndexed { index, _ ->
            val cardSize = animateDpAsState(targetValue = cardSizes[index]).value
            Column (
                modifier = Modifier
                    .clickable(
                        onClick = {
                            pageID.value = index
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
                AnimatedVisibility(visible = index == pageID.value) {
                    Text(
                        text = "selected",
                        modifier = Modifier
                            .padding(bottom = 5.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 应用底部导航栏
 *
 * 当显示应用的设备为竖屏或屏幕狭窄时，显示底部导航栏
 */
@Composable
fun bottomNavBar(pageID: MutableIntState) {
    val items = listOf("主页", "全部", "我的")
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEachIndexed { index, _ ->
            NavigationBarItem(
                icon = { NavigationIcon(index) },
                selected = index == pageID.intValue,
                onClick = {
                    pageID.value = index
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
        0 -> Icon(Icons.Filled.Edit, contentDescription = "Home", modifier = Modifier.padding(3.dp))
        1 -> Icon(Icons.Filled.Add, contentDescription = "Favorite", modifier = Modifier.padding(3.dp))
        else -> Icon(Icons.Filled.Person, contentDescription = "Person", modifier = Modifier.padding(3.dp))
    }
}

@Composable
fun customTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
//            background = Color(0xFF212028),
//            surfaceContainer = Color(0xFF141318)
        )
        else -> lightColorScheme(
//            background = Color(0xFFF3EDF7),
//            surfaceContainer = Color(0xFFFDF8FF)
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
expect fun getScheduleSDK(): ScheduleSDK