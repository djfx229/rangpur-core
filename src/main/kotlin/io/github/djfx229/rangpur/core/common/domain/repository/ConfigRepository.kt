package io.github.djfx229.rangpur.core.common.domain.repository

import io.github.djfx229.rangpur.core.common.domain.model.Config

/**
 * Его реализоации хранят в приватных полях объект T конфигурации, его поля mutable
 */
abstract class ConfigRepository<T : Config> {
    // загружает текущие данные конфигурации, не возвращает, для получения используются get() метод
    abstract fun load()

    abstract fun get(): T

    // сохраняет текущий configDto в файл
    abstract fun save()
}