package kmp.project.schedule.util.tokenUtil

import kmp.project.schedule.util.SettingsManager
import kmp.project.schedule.util.SettingsName

class AuthTokenManager(
    private val settingsManager: SettingsManager
) {
    fun getAccessToken(): String? {
        return settingsManager.getSetting(SettingsName.ACCESS_TOKEN.toString(), String::class.java)
    }

    fun getRefreshToken(): String? {
        return settingsManager.getSetting(SettingsName.REFRESH_TOKEN.toString(), String::class.java)
    }

    fun addToken(accessToken: String, refreshToken: String) {
        settingsManager.addSetting(SettingsName.ACCESS_TOKEN.toString(), accessToken)
        settingsManager.addSetting(SettingsName.REFRESH_TOKEN.toString(), refreshToken)
    }

    fun removeToken() {
        settingsManager.removeSetting(SettingsName.ACCESS_TOKEN.toString())
        settingsManager.removeSetting(SettingsName.REFRESH_TOKEN.toString())
    }
}