package kmp.project.schedule.ui.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.entity.LoginEntity
import kmp.project.schedule.ui.composableItem.LoadingDialog
import kmp.project.schedule.ui.userImage
import kotlinschedule.composeapp.generated.resources.Res
import kotlinschedule.composeapp.generated.resources.mdieye
import kotlinschedule.composeapp.generated.resources.mdieyeoff
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginPage(
    showLoadingDialog: MutableState<Boolean>,
    onLoginClick: (LoginEntity) -> Unit,
    onBackClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }

        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回"
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        userImage("test", 100.dp)

        //username text field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier
                .width(300.dp)
                .padding(top = 40.dp, bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        //password text field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .width(300.dp)
                .padding(bottom = 20.dp),
            trailingIcon = {
                IconButton(
                    onClick = { showPassword = !showPassword }
                ) {
                    Icon(
                        painter = painterResource(
                            if (showPassword)
                                Res.drawable.mdieye
                            else
                                Res.drawable.mdieyeoff
                        ),
                        contentDescription = "Show Password",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Button(
            onClick = { onLoginClick(LoginEntity(username, password)) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(300.dp)
                .height(55.dp)
        ) {
            Text(
                text = "登录",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "login"
            )
        }

        Text(
            text = "注册账号",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 30.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }

    if (showLoadingDialog.value) {
        LoadingDialog(
            title = "正在登录",
            onDismiss = {}
        )
    }

}