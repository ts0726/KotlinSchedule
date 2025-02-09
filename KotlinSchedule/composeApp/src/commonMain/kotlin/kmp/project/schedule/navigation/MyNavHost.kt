package kmp.project.schedule.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kmp.project.schedule.entity.NicknameRequest
import kmp.project.schedule.ui.my.LoginPage
import kmp.project.schedule.ui.my.accountPage
import kmp.project.schedule.ui.my.myPageContent
import kmp.project.schedule.util.tokenUtil.getUsernameFromToken
import kmp.project.schedule.util.viewUtil.sayHello
import kmp.project.schedule.viewModel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    showLoadingDialog: MutableState<Boolean>
) {
    NavHost(
        navController = navController,
        startDestination = "my",
        enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
            ) + slideIntoContainer(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                initialOffset = { fullSize -> fullSize / 3 }
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
            ) + slideOutOfContainer(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                targetOffset = { fullSize -> fullSize / 3 }
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
            ) + slideIntoContainer(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { fullSize -> fullSize / 3 }
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
            ) + slideOutOfContainer(
                animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.84f,0f,0f,0.98f)),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                targetOffset = { fullSize -> fullSize / 3 }
            )
        }
    ) {

        composable("my") {
            var hello by mutableStateOf("未登录")
            val token = authViewModel.getRefreshToken()
            if (token != null)
                hello = sayHello() + "，" + authViewModel.getNickname()
            myPageContent(
                hello,
                onSettingClicked = {},
                onAccountClicked = {
                    if (token != null)
                        navController.navigate("account")
                    else
                        navController.navigate("login")
                }
            )
        }
        composable("login") {
            LoginPage(
                showLoadingDialog = showLoadingDialog,
                onLoginClick = { loginEntity ->
                    showLoadingDialog.value = true
                    authViewModel.login(
                        loginEntity = loginEntity,
                        requestFinished = { showLoadingDialog.value = false }
                    )
                },
                onBackClicked = { navController.navigateUp() }
            )
        }
        composable("account") {
            val token = authViewModel.getAccessToken()
            var username = "未登录"
            val nickname by authViewModel.nicknameState.collectAsState()
            if (token != null) {
                username = getUsernameFromToken(token).toString()
            }
            accountPage(
                onBackClicked = { navController.navigateUp() },
                username = username,
                nickname = nickname,
                showLoadingDialog = showLoadingDialog,
                onSwitchAccountClicked = { navController.navigate("login") },
                onLogoutClicked = {
                    authViewModel.clearTokens()
                    authViewModel.clearNickname()
                    navController.navigateUp()
                },
                onUpdateNickname = {changedNickname ->
                    showLoadingDialog.value = true
                    val request = NicknameRequest(nickname = changedNickname)
                    if (token != null) {
                        authViewModel.updateNickname(
                            nicknameRequest = request,
                            token = token,
                            showSnackBar = { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        withDismissAction = true
                                    )
                                }
                            },
                            requestFinished = { showLoadingDialog.value = false }
                        )
                    }
                }
            )
        }
    }
}