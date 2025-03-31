package kmp.project.schedule.util

import android.os.Build

actual object DeviceUtil {
    actual fun getDeviceName(): String {
        return Build.BRAND + "手机"
    }
}