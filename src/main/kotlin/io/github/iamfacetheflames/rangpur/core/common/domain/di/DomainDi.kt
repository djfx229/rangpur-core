package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.common.domain.model.Config
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class DomainDi : DependencyInjector<Any>() {
    fun <T : Config> getConfigRepository(clazz: KClass<T>): ConfigRepository<T> {
        return get(ConfigRepository::class, dependencyName = clazz.qualifiedName) as ConfigRepository<T>
    }
}