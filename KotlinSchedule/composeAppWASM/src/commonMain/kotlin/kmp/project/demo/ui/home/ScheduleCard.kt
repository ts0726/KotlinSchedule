package kmp.project.demo.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

/**
 * 日程显示卡片
 *
 * 日程信息在卡片中显示，通过点击卡片可查看对应日程
 */
@Composable
fun scheduleCard(
    title: String,
    content: String,
    navHostController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navHostController.navigate("scheduleDetail" +
                    "/$title" +
                    "/$content"
            ) {
                //清除栈中的日程详情页面，防止叠加
                popUpTo("scheduleDetail/{title}/{content}") {
                    inclusive = true
                }
            }
        }
    ) {
        Column {
            scheduleCard_Content(title, content)
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
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            fontSize = 15.sp,
            textAlign = TextAlign.Justify,
            maxLines = 5
        )
    }
}