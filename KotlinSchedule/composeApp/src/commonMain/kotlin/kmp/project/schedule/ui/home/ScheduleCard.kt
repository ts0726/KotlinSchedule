package kmp.project.schedule.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.database.Schedule

/**
 * 日程显示卡片
 *
 * 日程信息在卡片中显示，通过点击卡片可查看对应日程
 */
@Composable
fun scheduleCard(
    schedule: Schedule,
    onCardClick: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = { onCardClick(schedule.uuid) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
    ) {
        Column {
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
            .padding(10.dp)
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