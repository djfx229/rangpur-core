package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.model.Config
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import kotlin.reflect.KClass

abstract class DependencyInjector {

    private val dependencies = mutableMapOf<String, () -> Any>()

    /**
     * Предпочтительнее использовать inline метод для получения зависимости.
     *
     * Данный же метод с публичным доступом останется, потому что у inline функции нет доступа к приватным методам, но
     * даже если бы он был - inline функции адекватно не проверить из unit-тестов.
     */
    fun <T : Any> get(
        classType: KClass<T>,
        dependencyName: String? = null,
    ): T {
        val key = classType.simpleName!! + (dependencyName ?: "")
        return dependencies.get(key)!!.invoke() as T
    }

    inline fun <reified T : Any> get(
        dependencyName: String? = null,
    ): T {
        return get(
            classType = T::class,
            dependencyName,
        )
    }

    fun <T : Any> add(
        classType: KClass<T>,
        dependencyName: String? = null,
        callbackForBuild: () -> T
    ) {
        saveDependency(classType, dependencyName, callbackForBuild = callbackForBuild)
    }

    fun <T : Any> addSingleton(
        classType: KClass<T>,
        dependency: T,
        dependencyName: String? = null,
    ) {
        saveDependency(classType, dependencyName) { dependency }
    }

    private fun <T : Any> saveDependency(
        classType: KClass<T>,
        dependencyName: String? = null,
        callbackForBuild: () -> T,
    ) {
        val key = classType.simpleName!! + (dependencyName ?: "")
        if (dependencies[key] != null) throw IllegalStateException("Зависимость $key уже зарегестрирована")
        dependencies.put(
            key = key,
            value = callbackForBuild
        )
    }

}

inline fun <reified T : Config> DependencyInjector.getConfigRepository(): ConfigRepository<T> {
    return get<ConfigRepository<T>>(dependencyName = T::class.qualifiedName)
}