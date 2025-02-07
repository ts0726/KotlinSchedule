package kmp.project.schedule.util.tokenUtil

import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.util.SettingsName

class AuthTokenManager(private val sdk: ScheduleSDK) {
    fun getAccessToken(): String? {
        return sdk.getSetting(SettingsName.ACCESS_TOKEN.toString(), String::class.java)
    }

    fun getRefreshToken(): String? {
        return sdk.getSetting(SettingsName.REFRESH_TOKEN.toString(), String::class.java)
    }

    fun addToken(accessToken: String, refreshToken: String) {
        sdk.addSetting(SettingsName.ACCESS_TOKEN.toString(), accessToken)
        sdk.addSetting(SettingsName.REFRESH_TOKEN.toString(), accessToken)
    }

    fun removeToken() {
        sdk.removeSetting(SettingsName.ACCESS_TOKEN.toString())
        sdk.removeSetting(SettingsName.REFRESH_TOKEN.toString())
    }
}