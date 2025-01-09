package kmp.project.schedule.util

import androidx.compose.runtime.Composable

enum class ReorderHapticFeedbackType {
    START,
    MOVE,
    END,
}

open class ReorderHapticFeedback {
    open fun performHapticFeedback(type: ReorderHapticFeedbackType) {
        //no op
    }
}

@Composable
expect fun rememberReorderHapticFeedback(): ReorderHapticFeedback