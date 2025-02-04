package kmp.project.schedule.ui.my

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun accountPage(
    onBackClicked: () -> Unit,
    username: String,
    nickname: String,
    onSwitchAccountClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onUpdateNickname: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showEditNicknameDialog by remember { mutableStateOf(false) }

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
            nickname = nickname,
            onNicknameClicked = { showEditNicknameDialog = true },
            onSwitchAccountClicked = onSwitchAccountClicked,
            onLogoutClicked = onLogoutClicked
        )
    }

    if (showEditNicknameDialog) {
        editNicknameDialog(
            nickname = nickname,
            onDismiss = { showEditNicknameDialog = false },
            onUpdateNickname = onUpdateNickname
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun accountSettings(
    innerPadding: PaddingValues,
    username: String,
    nickname: String,
    onNicknameClicked: () -> Unit,
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
                        onClick = onNicknameClicked
                    )
            ) {
                Text(
                    text = "昵称",
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = nickname,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun editNicknameDialog(
    nickname: String,
    onDismiss: () -> Unit,
    onUpdateNickname: (String) -> Unit
) {
    var tempNickname by remember { mutableStateOf(nickname) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column( modifier = Modifier.padding(10.dp) ) {
                Text(
                    text = "编辑昵称",
                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 20.dp)
                )
                OutlinedTextField(
                    value = tempNickname,
                    onValueChange = { tempNickname = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "取消",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    TextButton(onClick = { onUpdateNickname(tempNickname) }) {
                        Text(
                            text = "确定",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}