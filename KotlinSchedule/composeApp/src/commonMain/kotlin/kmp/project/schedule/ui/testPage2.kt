package kmp.project.schedule.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TestPage2() {
    Text(
        text = "Test Page 2",
        modifier = Modifier
            .fillMaxSize(),
        textAlign = TextAlign.Center
    )
}