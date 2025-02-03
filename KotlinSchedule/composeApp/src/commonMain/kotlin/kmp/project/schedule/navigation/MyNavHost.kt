package kmp.project.schedule.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.ui.my.LoginPage
import kmp.project.schedule.ui.my.accountPage
import kmp.project.schedule.ui.my.myPageContent
import kmp.project.schedule.util.SettingsName
import kmp.project.schedule.util.getUsernameFromToken
import kmp.project.schedule.util.sayHello
import kmp.project.schedule.viewModel.AuthViewModel

@Composable
fun MyNavHost(
    navController: NavHostController,
    sdk: ScheduleSDK,
    authViewModel: AuthViewModel,
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
            val token = sdk.getSetting(SettingsName.REFRESH_TOKEN.toString())
            if (token != null)
                hello = sayHello() + "，" + sdk.getSetting(SettingsName.NICKNAME.toString())
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
                onLoginClick = { loginEntity ->
                    authViewModel.login(loginEntity)
                },
                onBackClicked = { navController.navigateUp() }
            )
        }
        composable("account") {
            val token = sdk.getSetting(SettingsName.REFRESH_TOKEN.toString())
            var username = "未登录"
            var nickname = "未登录"
            if (token != null) {
                username = getUsernameFromToken(token).toString()
                nickname = sdk.getSetting(SettingsName.NICKNAME.toString())!!
            }
            accountPage(
                onBackClicked = { navController.navigateUp() },
                username = username,
                nickname = nickname,
                onSwitchAccountClicked = { navController.navigate("login") },
                onLogoutClicked = {
                    sdk.removeSetting(SettingsName.ACCESS_TOKEN.toString())
                    sdk.removeSetting(SettingsName.REFRESH_TOKEN.toString())
                    navController.navigateUp()
                }
            )
        }
    }
}