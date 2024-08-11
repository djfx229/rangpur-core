package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.common.domain.model.Config
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class DomainDi : DependencyInjector<Any>() {
    @Deprecated("Используй inline версию данной функции")
    fun <T : Config> getConfigRepository(clazz: KClass<T>): ConfigRepository<T> {
        return get<ConfigRepository<T>>(dependencyName = clazz.qualifiedName)
    }

    inline fun <reified T : Config> getConfigRepository(): ConfigRepository<T> {
        return get<ConfigRepository<T>>(dependencyName = T::class.qualifiedName)
    }
}