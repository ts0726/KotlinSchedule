package kmp.project.schedule.util

import com.russhwolf.settings.Settings

/**
 * A manager class for handling settings using the provided [com.russhwolf.settings.Settings] instance.
 *
 * This class provides methods to add, retrieve, and remove settings in a type-safe manner.
 *
 * @param settings The [com.russhwolf.settings.Settings] instance used for managing the settings.
 */
class SettingsManager(private val settings: Settings) {

    /**
     * Adds a setting with the specified key and value.
     *
     * The method determines the type of the value and stores it accordingly in the [Settings] instance.
     *
     * @param key The key for the setting.
     * @param value The value of the setting, which can be of type String, Int, Boolean, Float, or Long.
     * @throws IllegalArgumentException If the value type is unsupported.
     */
    fun addSetting(key: String, value: Any) {
        when (value) {
            is String -> settings.putString(key, value)
            is Int -> settings.putInt(key, value)
            is Boolean -> settings.putBoolean(key, value)
            is Float -> settings.putFloat(key, value)
            is Long -> settings.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported types: ${value::class}")
        }
    }

    /**
     * Retrieves a setting value for the specified key and type.
     *
     * The method checks the type of the requested setting and retrieves it from the [Settings] instance.
     *
     * @param key The key for the setting to retrieve.
     * @param classType The expected type of the setting value (e.g., String::class.java, Int::class.java).
     * @return The value of the setting if it exists and matches the expected type, or null otherwise.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSetting(key: String, classType: Class<T>): T? {
        return when (classType) {
            String::class.java -> settings.getStringOrNull(key) as T
            Int::class.java -> settings.getIntOrNull(key) as T
            Boolean::class.java -> settings.getBooleanOrNull(key) as T
            Float::class.java -> settings.getFloatOrNull(key) as T
            Long::class.java -> settings.getLongOrNull(key) as T
            else -> null
        }
    }

    /**
     * Removes a setting with the specified key.
     *
     * This method deletes the setting associated with the given key from the [Settings] instance.
     *
     * @param key The key for the setting to remove.
     */
    fun removeSetting(key: String) {
        settings.remove(key)
    }
}