package kmp.project.schedule.ui.my

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kmp.project.schedule.navigation.MyNavHost
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.ui.userImage
import kmp.project.schedule.viewModel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun myPage(
    navHostController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val result = authState) {
            is ApiResult.Success -> {
                authViewModel.updateTokens(result.data.accessToken, result.data.refreshToken)
                authViewModel.updateNickname(result.data.nickname)
                authViewModel.resetNickname()   //每次登录手动更新一下nickname，不然在账号管理界面中昵称会不更新
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = "登录成功", withDismissAction = true)
                }
                withContext(Dispatchers.Main) {
                    navHostController.navigateUp()
                }
                authViewModel.resetTokenState() //刷新登录状态
            }
            is ApiResult.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "登录失败：${result.message}",
                        withDismissAction = true
                    )
                }
                authViewModel.resetTokenState() //刷新登录状态
            }
            null -> {}
        }
    }

    MyNavHost(
        navController = navHostController,
        authViewModel = authViewModel,
        snackbarHostState = snackbarHostState,
        coroutineScope = coroutineScope
    )
}

@Composable
fun myPageContent(
    hello: String,
    onSettingClicked: () -> Unit,
    onAccountClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 60.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            userImage("test", 35.dp)
            Text(
                text = hello,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 30.dp),
                lineHeight = 40.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 30.dp)
        ) {
            item {
                myCard(
                    icon = Icons.Default.Settings,
                    title = "设置",
                    myCardClicked = onSettingClicked
                )
            }
            item {
                myCard(
                    icon = Icons.Filled.AccountCircle,
                    title = "账号管理",
                    myCardClicked = onAccountClicked
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "版本：0.0.1 alpha",
            fontSize = 10.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun myCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    myCardClicked: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = myCardClicked
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title)
            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}