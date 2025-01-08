package kmp.project.schedule.util

import androidx.compose.runtime.Composable

enum class ReorderHapticFeedbackType {
    START,
    MOVE,
    END,
}

open class ReorderHapticFeedback {
    open fun performHapticFeedback(type: ReorderHapticFeedbackType) {
        when (type) {
            ReorderHapticFeedbackType.START -> {
                // Handle start feedback
            }
            ReorderHapticFeedbackType.MOVE -> {
                // Handle move feedback
            }
            ReorderHapticFeedbackType.END -> {
                // Handle end feedback
                // Add your end feedback logic here
            }
        }
    }
}

@Composable
expect fun rememberReorderHapticFeedback(): ReorderHapticFeedback