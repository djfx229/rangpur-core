package io.github.iamfacetheflames.rangpur.core.common.domain.di

import kotlin.reflect.KClass

abstract class DependencyInjector<TypeDependency : Any> {
    private val dependencies = mutableMapOf<String, () -> TypeDependency>()

    @Deprecated("Используй inline версию данной функции")
    fun <T : TypeDependency> get(
        classType: KClass<T>,
        dependencyName: String? = null,
    ): T {
        val key = classType.simpleName!! + (dependencyName ?: "")
        return dependencies.get(key)!!.invoke() as T
    }

    inline fun <reified T : TypeDependency> get(
        dependencyName: String? = null,
    ): T {
        return get(
            classType = T::class,
            dependencyName,
        )
    }

    fun <T : TypeDependency> add(
        classType: KClass<T>,
        dependencyName: String? = null,
        callbackForBuild: () -> T
    ) {
        saveDependency(classType, dependencyName, callbackForBuild = callbackForBuild)
    }

    fun <T : TypeDependency> addSingleton(
        classType: KClass<T>,
        dependency: T,
        dependencyName: String? = null,
    ) {
        saveDependency(classType, dependencyName) { dependency }
    }

    private fun <T : TypeDependency> saveDependency(
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

