package kmp.project.demo

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
    ) {
        App()
    }
}

//actual fun name(): String = "Desktop"

actual fun getSystemFontFamily(): FontFamily {
    return FontFamily.Default
}