package kmp.project.schedule

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import kmp.project.schedule.di.appModule
import kmp.project.schedule.di.repositoryModule
import kmp.project.schedule.di.settingsModule
import kmp.project.schedule.di.viewModelModule
import org.koin.compose.KoinApplication

@Composable
actual fun PlatformKoinApplication(content: @Composable () -> Unit) {
    KoinApplication(
        application = {
            //通用模块
            modules(appModule, viewModelModule)
            //平台特定模块
            modules(repositoryModule, settingsModule)
        }
    ) {
        println("Desktop KoinApplication initialized")
        content()
    }
}

@Composable
actual fun PlatformTheme(
    darkTheme: Boolean,
    content: @Composable (() -> Unit)
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}