package kmp.project.schedule.ui.my

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun accountPage(
    onBackClicked: () -> Unit,
    username: String,
    onSwitchAccountClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "账号管理",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton( onClick = onBackClicked ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {innerPadding ->
        accountSettings(
            innerPadding = innerPadding,
            username = username,
            onSwitchAccountClicked = onSwitchAccountClicked,
            onLogoutClicked = onLogoutClicked
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun accountSettings(
    innerPadding: PaddingValues,
    username: String,
    onSwitchAccountClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(innerPadding)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "用户名",
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = username,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = onSwitchAccountClicked
                    )
            ) {
                Text(
                    text = "切换账号",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = onLogoutClicked
                    )
            ) {
                Text(
                    text = "退出登录",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}