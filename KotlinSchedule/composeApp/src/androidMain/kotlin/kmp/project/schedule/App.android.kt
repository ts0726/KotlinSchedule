package kmp.project.schedule

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