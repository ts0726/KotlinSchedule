package kmp.project.schedule.ui.composableItem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.net.SseConnectionStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun SseConnectionStatusIndicatorPreview() {
    SseConnectionStatusIndicator(SseConnectionStatus.CONNECTING, onClick =  {})
}

@Composable
fun SseConnectionStatusIndicator(
    connectionStatus: SseConnectionStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = sseConnectionStatusBackgroundColor(connectionStatus),
        animationSpec = tween(durationMillis = 300),
        label = "background_color"
    )

    val borderColor by animateColorAsState(
        targetValue = sseConnectionBorderColor(connectionStatus),
        animationSpec = tween(durationMillis = 300),
        label = "border_color"
    )

    val textColor by animateColorAsState(
        targetValue = sseConnectionStatusTextColor(connectionStatus),
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )

    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center,

    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = connectionStatus,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "status_icon"
            ) { status ->
                SseConnectionStatusIcon(status)
            }
            Text(
                modifier = Modifier
                    .padding(start = 7.dp),
                text = SseConnectionStatusText(connectionStatus),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
    }
}

fun SseConnectionStatusText(status: SseConnectionStatus) : String {
    return when (status) {
        SseConnectionStatus.CONNECTED -> "已就绪"
        SseConnectionStatus.CONNECTING -> "连接中"
        SseConnectionStatus.CLOSED -> "已断开"
    }
}

@Composable
fun sseConnectionStatusTextColor(status: SseConnectionStatus) : Color {
    return when (status) {
        SseConnectionStatus.CONNECTED -> if (isSystemInDarkTheme()) Color(0xFFc1f0a4) else Color(0xFF072100)
        SseConnectionStatus.CONNECTING -> if (isSystemInDarkTheme()) Color(0xFFFFF9C4) else Color(0xFF572908)
        SseConnectionStatus.CLOSED -> MaterialTheme.colorScheme.onError
    }
}

@Composable
fun sseConnectionStatusBackgroundColor(status: SseConnectionStatus) : Color {
    return when (status) {
        SseConnectionStatus.CONNECTED -> if (isSystemInDarkTheme()) Color(0xFF072100) else Color(0xFFc1f0a4)
        SseConnectionStatus.CONNECTING -> if (isSystemInDarkTheme()) Color(0xFF572908) else Color(0xFFFFF9C4)
        SseConnectionStatus.CLOSED -> MaterialTheme.colorScheme.error
    }
}

@Composable
fun sseConnectionBorderColor(status: SseConnectionStatus) : Color {
    return when (status) {
        SseConnectionStatus.CONNECTED -> if (isSystemInDarkTheme()) Color(0xFFc1f0a4) else Color(0xFF072100)
        SseConnectionStatus.CONNECTING -> if (isSystemInDarkTheme()) Color(0xFFFFF9C4) else Color(0xFF572908)
        SseConnectionStatus.CLOSED -> MaterialTheme.colorScheme.error
    }
}

@Composable
fun SseConnectionStatusIcon(status: SseConnectionStatus) {
    when (status) {
        SseConnectionStatus.CONNECTED -> {
            Icon(
                modifier = Modifier
                    .width(17.dp)
                    .height(17.dp),
                tint = if (isSystemInDarkTheme()) Color(0xFFc1f0a4) else Color(0xFF072100),
                imageVector = Icons.Filled.Check,
                contentDescription = "Connected"
            )
        }
        SseConnectionStatus.CONNECTING -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(17.dp)
                    .height(17.dp),
                color = if (isSystemInDarkTheme()) Color(0xFFFFF9C4) else Color(0xFF572908),
                strokeWidth = 2.dp
            )
        }
        SseConnectionStatus.CLOSED -> {
            Icon(
                modifier = Modifier
                    .width(17.dp)
                    .height(17.dp),
                tint = MaterialTheme.colorScheme.onError,
                imageVector = Icons.Filled.Close,
                contentDescription = "Closed"
            )
        }
    }
}