package io.github.djfx229.rangpur.core.common.domain.di

import kotlin.reflect.KClass

/**
 * Обёртка для DependencyInjector, реализующая кеширование получаемых из DI зависимостей.
 *
 * Позволяет гарантировано получать одни и те же объекты для зависимостей, не прибегая к регистрации singleton.
 * Важно осуществлять очистку кэша [release], когда в текущем объекте CachedDi больше нет необходимости.
 */
class CachedDependencyInjector(
    private val di: DependencyInjector,
) {

    private val cachedDependencies: MutableMap<String, Any> = mutableMapOf()
    private var isReleased = false

    inline fun <reified T : Any> get(
        dependencyName: String? = null,
    ): T {
        return get(
            classType = T::class,
            dependencyName,
        )
    }

    inline fun <reified T : Any> add(
        dependencyObject: T,
        dependencyName: String? = null,
    ) {
        add(T::class, dependencyObject, dependencyName)
    }

    fun release() {
        isReleased = true
        cachedDependencies.clear()
    }

    // Был бы этот метод приватным, но из inline функции нельзя обращаться к приватным полям.
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(
        classType: KClass<T>,
        dependencyName: String? = null,
    ): T {
        if (isReleased) throw IllegalStateException("Данный DiScope уже очищен и не может использоваться для получения зависимостей")
        return cachedDependencies.getOrPut(
            key = classType.simpleName!! + (dependencyName ?: ""),
            defaultValue = {
                di.get(classType, dependencyName)
            },
        ) as T
    }

    // Был бы этот метод приватным, но из inline функции нельзя обращаться к приватным полям.
    fun < T : Any> add(
        classType: KClass<T>,
        dependencyObject: T,
        dependencyName: String? = null,
    ) {
        val key = classType.simpleName!! + (dependencyName ?: "")
        cachedDependencies[key] = dependencyObject
    }

}