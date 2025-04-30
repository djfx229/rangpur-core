package io.github.djfx229.rangpur.core.common.domain.util

import java.util.Locale

object DurationFormat {

    /**
     * Форматирует время в строку.
     *
     * Формат 0:00. Если время превышает час, то формат 0:00:00.
     * Исходный код: https://stackoverflow.com/a/72653862
     */
    fun format(timeSec: Long?): String {
        if (timeSec == null || timeSec < 0) return ""
        return if (timeSec >= 60 * 60) {
            String.format(Locale.US, "%d:%02d:%02d", timeSec / 3600, timeSec % 3600 / 60, timeSec % 60)
        } else {
            String.format(Locale.US, "%d:%02d",  timeSec % 3600 / 60, timeSec % 60)
        }
    }

}