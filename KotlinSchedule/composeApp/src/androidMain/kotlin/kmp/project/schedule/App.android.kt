package kmp.project.schedule

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kmp.project.schedule.di.appModule
import kmp.project.schedule.di.repositoryModule
import kmp.project.schedule.di.settingsModule
import kmp.project.schedule.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
actual fun PlatformKoinApplication(content: @Composable () -> Unit) {
    val context = LocalContext.current
    KoinApplication(
        application = {
            androidContext(context)
            //通用模块
            modules(appModule, viewModelModule)
            //平台相关的模块
            modules(repositoryModule, settingsModule)
        }
    ) {
        content()
    }
}

@Composable
actual fun PlatformTheme(
    darkTheme: Boolean,
    content: @Composable (() -> Unit)
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}