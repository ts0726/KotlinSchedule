package kmp.project.schedule.db

import com.russhwolf.settings.Settings

expect class SettingsFactory {
    fun createSettings(): Settings
}