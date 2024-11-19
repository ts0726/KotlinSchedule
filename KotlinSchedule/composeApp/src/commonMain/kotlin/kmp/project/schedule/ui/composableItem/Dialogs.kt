package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun dialogPreview() {
//    CalendarPickerDialog({}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    date: MutableState<LocalDate>
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        var localDate = date.value
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onBackground,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            ModalBottomSheetTitle(
                title = "日期选择",
                onConfirm = {
                    onDateSelected(localDate)
                    onDismiss()
                }
            )
            CalendarPager(localDate) { date ->
                localDate = date
            }
        }
    }
}

@Composable
fun ModalBottomSheetTitle(
    title: String,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 15.sp
        )
        IconButton(
            onClick = onConfirm,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirm",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
