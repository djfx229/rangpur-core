package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.Logger
import io.github.iamfacetheflames.rangpur.core.common.domain.model.CoreConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.repository.database.Database

/**
 * Метод, осуществляющий регистрацию зависимостей необходимых для классов из core.
 *
 * Создан в связи с необходимостью иметь возможность в runtime подсветить отсутствующие зависимости из core.
 */
fun DependencyInjector.registryCoreDependencies(
    logger: Logger,
    database: Database,
    configRepository: ConfigRepository<CoreConfig>,
) {
    addSingleton(
        Logger::class,
        logger,
    )
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