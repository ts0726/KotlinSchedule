package kmp.project.schedule.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.util.viewUtil.ReorderHapticFeedback
import kmp.project.schedule.util.viewUtil.ReorderHapticFeedbackType
import sh.calvin.reorderable.ReorderableCollectionItemScope

/**
 * 日程显示卡片
 *
 * 日程信息在卡片中显示，通过点击卡片可查看对应日程
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun scheduleCard(
    modifier: Modifier = Modifier,
    schedule: Schedule,
    isSelected: Boolean,
    onCardClick: (String) -> Unit,
    onCardLongClick: (String) -> Unit,
    scope: ReorderableCollectionItemScope,
    haptic: ReorderHapticFeedback,
    onDragStopped: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = with(scope) {
            modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp, start = 15.dp, end = 15.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onLongClick = { onCardLongClick(schedule.uuid) },
                    onClick = { onCardClick(schedule.uuid) },
                )
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .longPressDraggableHandle(
                    onDragStarted = {
                        haptic.performHapticFeedback(ReorderHapticFeedbackType.START)
                    },
                    onDragStopped = {
                        onDragStopped()
                    },
                )
        }

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            scheduleCard_Content(schedule.title, schedule.content!!)
        }
    }
}

/**
 * 日程卡片内容
 */
@Composable
fun scheduleCard_Content(title: String, content: String) {
    Column(
        modifier = Modifier
            .padding(10.dp),
    ) {
        Text(
            text = title,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = content,
            fontSize = 15.sp,
            textAlign = TextAlign.Justify,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}