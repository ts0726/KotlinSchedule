package kmp.project.schedule.util

actual object DeviceUtil {
    actual fun getDeviceName(): String {
        return System.getProperty("os.name") + "电脑"
    }
}