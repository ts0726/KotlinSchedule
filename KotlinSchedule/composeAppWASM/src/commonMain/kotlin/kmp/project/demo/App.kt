package kmp.project.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kmp.project.demo.model.NewScheduleViewModel
import kmp.project.demo.theme.Typography
import kmp.project.demo.ui.TestPage1
import kmp.project.demo.ui.TestPage2
import kmp.project.demo.ui.home.mainPage
import kmp.project.demo.ui.userImage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun appPreview() {
    CustomScaffold(true)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    val windowSize = calculateWindowSizeClass()
    val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    customTheme {
        CustomScaffold(isCompact = isCompact)
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
fun CustomScaffold(isCompact: Boolean) {
    val pageID = remember{ mutableIntStateOf(0) }
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
                //保存控件状态
                val navController = rememberNavController()
                val listState = rememberLazyListState()
                val viewModel: NewScheduleViewModel = viewModel{ NewScheduleViewModel() }
                contentContainer(
                    content = {
                        when (pageID.value) {
                            0 -> mainPage(
                                isCompact = isCompact,
                                navController = navController,
                                listState = listState,
                                viewModel = viewModel
                            )
                            1 -> TestPage1()
                            2 -> TestPage2()
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
            .padding(padding),
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
            userImage("testURL")
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
        contentColor = MaterialTheme.colorScheme.error,
    ) {
        items.forEachIndexed { index, _ ->
            NavigationBarItem(
                icon = { NavigationIcon(index) },
                selected = index == pageID.intValue,
                onClick = {
                    pageID.value = index
                }
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
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = Typography
    )
}

/**
 * 根据系统获取字体，安卓和桌面端使用默认字体，web端使用MiSans字体
 *
 * 由于当前Kotlin wasm中文显示有问题，因此需要加载本地字体来显示中文
 */
expect fun getSystemFontFamily(): FontFamily