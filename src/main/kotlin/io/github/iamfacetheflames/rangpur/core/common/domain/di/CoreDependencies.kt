package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.model.CoreConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.repository.database.Database

/**
 * Класс, упрощающий регистрацию зависимостей необходимых для классов из core.
 *
 * Создан в связи с необходимостью иметь возможность в runtime подсветить отсутствующие зависимости.
 */
object CoreDependencies {
    fun DomainDi.registryCore(
        database: Database,
        configRepository: ConfigRepository<CoreConfig>,
    ) {
        addSingleton(
            ConfigRepository::class,
            dependencyName = CoreConfig::class.qualifiedName,
            dependency = configRepository,
        )
        addSingleton(
            Database::class,
            database
        )
    }
}