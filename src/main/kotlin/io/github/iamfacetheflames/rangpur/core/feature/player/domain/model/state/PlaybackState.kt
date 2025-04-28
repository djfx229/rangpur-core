package io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.state

sealed class PlaybackState {
    object Playing : PlaybackState()

    object Paused : PlaybackState()

    object Stopped : PlaybackState()

    /**
     * Позволяет проверить, не находится ли плеер в состоянии буфферизации.
     *
     * Пример использования: игнорировать запрос данных о позиции трека в момент буфферизации.
     */
    object Buffering : PlaybackState()

    /**
     * Неизвестное состояние, например когда запрос статуса произошёл у не проинициализированного плеера
     */
    object Unknown : PlaybackState()
}
