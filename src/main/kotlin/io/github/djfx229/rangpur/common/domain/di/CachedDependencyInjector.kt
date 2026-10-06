package io.github.djfx229.rangpur.common.domain.di

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
        ignoreCache: Boolean = false,
    ): T {
        return get(
            classType = T::class,
            dependencyName,
            ignoreCache,
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
        ignoreCache: Boolean = false,
    ): T {
        return if (ignoreCache) {
            di.get(classType, dependencyName)
        } else {
            if (isReleased) throw IllegalStateException("Данный CachedDependencyInjector уже очищен и не может использоваться для получения зависимостей")
            return cachedDependencies.getOrPut(
                key = classType.simpleName!! + (dependencyName ?: ""),
                defaultValue = {
                    di.get(classType, dependencyName)
                },
            ) as T
        }
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