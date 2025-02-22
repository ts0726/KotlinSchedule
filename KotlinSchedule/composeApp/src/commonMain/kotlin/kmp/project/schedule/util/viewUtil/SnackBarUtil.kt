package kmp.project.schedule.util.viewUtil

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun showSnackBar(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    message: String
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(
            message = message,
            withDismissAction = true
        )
    }
}