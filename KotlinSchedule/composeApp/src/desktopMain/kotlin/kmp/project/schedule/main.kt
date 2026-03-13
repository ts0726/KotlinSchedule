package kmp.project.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

//import java.awt.Color

fun main() = application {
    val onTop = remember { mutableStateOf(false) }
    val state = rememberWindowState(
        size = DpSize(1500.dp, 800.dp),
        placement = WindowPlacement.Floating
    )

    // 获取屏幕信息
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val gd = ge.defaultScreenDevice
    val screenBounds = gd.defaultConfiguration.bounds
    val insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.defaultConfiguration)
    val maxWidth = screenBounds.width - insets.left - insets.right
    val maxHeight = screenBounds.height - insets.top - insets.bottom

    // 最大化状态
    val isMaximized = remember { mutableStateOf(false) }
    val savedSize = remember { mutableStateOf<DpSize?>(null) }
    val savedPosition = remember { mutableStateOf<WindowPosition?>(null) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "日程",
        state = state,
        alwaysOnTop = onTop.value,
        undecorated = true,
        transparent = true,
    ) {

        //监听窗口状态
        LaunchedEffect(state) {
            snapshotFlow { state.position }
                .collect { newPosition ->
                    //全屏时移动窗口切回小屏
                    if (isMaximized.value &&
                        newPosition != WindowPosition.Absolute(insets.left.dp, insets.top.dp)) {
                        isMaximized.value = false
                        state.placement = WindowPlacement.Floating
                        state.size = savedSize.value!!
                    }
                }
        }

        Column(
            Modifier.clip(RoundedCornerShape(if (!isMaximized.value) 20.dp else 0.dp))
        ) {
            PlatformTheme{
                WindowDraggableArea {
                    Box(Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(15.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Schedule For Desktop",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //窗口置顶按钮
                                IconButton(
                                    onClick = {onTop.value = !onTop.value}
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(2.dp),
                                        imageVector = Icons.Filled.PushPin,
                                        tint = if (onTop.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "top"
                                    )
                                }

                                //最小化按钮
                                IconButton(
                                    onClick = {
                                        state.isMinimized = !state.isMinimized
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Minimize,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "minimize"
                                    )
                                }

                                //最大化按钮
                                key(isMaximized.value) {
                                    IconButton(
                                        onClick = {
                                            if (!isMaximized.value) {
                                                savedSize.value = state.size
                                                savedPosition.value = state.position
                                                state.size = DpSize(maxWidth.dp, maxHeight.dp)
                                                state.position = WindowPosition.Absolute(insets.left.dp, insets.top.dp)
                                                isMaximized.value = true
                                            } else {
                                                savedSize.value?.let { state.size = it }
                                                savedPosition.value?.let { state.position = it }
                                                isMaximized.value = false
                                            }
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.padding(2.dp),
                                            imageVector = if (!isMaximized.value) Icons.Filled.CropSquare else Icons.Filled.FilterNone,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            contentDescription = "maximize"
                                        )
                                    }
                                }

                                //退出按钮
                                IconButton(
                                    onClick = {
                                        exitApplication()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "close"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            App()
        }
    }
}