package kmp.project.schedule.navigation.navDisplay

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kmp.project.schedule.entity.NicknameRequest
import kmp.project.schedule.navigation.key.AccountPage
import kmp.project.schedule.navigation.key.LoginPage
import kmp.project.schedule.navigation.key.MyPage
import kmp.project.schedule.ui.my.LoginPage
import kmp.project.schedule.ui.my.MyPageContent
import kmp.project.schedule.util.tokenUtil.getUsernameFromToken
import kmp.project.schedule.util.viewUtil.sayHello
import kmp.project.schedule.util.viewUtil.showSnackBar
import kmp.project.schedule.viewModel.AuthViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MyNavDisplay(
    backStack: SnapshotStateList<Any>,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    showLoadingDialog: MutableState<Boolean>
) {
    val listDetailStrategy = rememberListDetailSceneStrategy<Any>()

    NavDisplay(
        sceneStrategy = listDetailStrategy,
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->
            when (key) {
                is MyPage -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // 占位界面
                            }
                        }
                    )
                ) {
                    var hello by mutableStateOf("未登录")
                    val token = authViewModel.getRefreshToken()
                    if (token != null)
                        hello = sayHello() + "，" + authViewModel.getNickname()
                    MyPageContent(
                        hello,
                        onSettingClicked = {},
                        onAccountClicked = {
                            if (token != null && backStack.last() != AccountPage)
                                backStack.add(AccountPage)
                            else if (token == null && backStack.last() != LoginPage)
                                backStack.add(LoginPage)
                        }
                    )
                }

                is LoginPage -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    LoginPage(
                        showLoadingDialog = showLoadingDialog,
                        onLoginClick = { loginEntity ->
                            showLoadingDialog.value = true
                            authViewModel.login(
                                loginEntity = loginEntity,
                                requestFinished = { showLoadingDialog.value = false }
                            )
                        },
                        onBackClicked = { backStack.removeLastOrNull() }
                    )
                }

                is AccountPage -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    val token = authViewModel.getAccessToken()
                    var username = "未登录"
                    val nickname by authViewModel.nicknameState.collectAsState()
                    if (token != null) {
                        username = getUsernameFromToken(token).toString()
                    }
                    kmp.project.schedule.ui.my.AccountPage(
                        onBackClicked = { backStack.removeLastOrNull() },
                        username = username,
                        nickname = nickname,
                        showLoadingDialog = showLoadingDialog,
                        onSwitchAccountClicked = { backStack.add(LoginPage) },
                        onLogoutClicked = {
                            authViewModel.clearTokens()
                            authViewModel.clearNickname()
                            backStack.removeLastOrNull()
                        },
                        onUpdateNickname = { changedNickname ->
                            showLoadingDialog.value = true
                            val request = NicknameRequest(nickname = changedNickname)
                            if (token != null) {
                                authViewModel.updateNickname(
                                    nicknameRequest = request,
                                    showSnackBar = {
                                        showSnackBar(
                                            snackbarHostState,
                                            coroutineScope,
                                            it
                                        )
                                    },
                                    requestFinished = { showLoadingDialog.value = false }
                                )
                            }
                        }
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