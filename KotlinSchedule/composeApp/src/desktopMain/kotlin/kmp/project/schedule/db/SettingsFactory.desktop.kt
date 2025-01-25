package kmp.project.schedule.db

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual class SettingsFactory {
    actual fun createSettings(): Settings {
        val delegate: Preferences = Preferences.userRoot().node("kmp.project.schedule")
        return PreferencesSettings(delegate)
    }
}