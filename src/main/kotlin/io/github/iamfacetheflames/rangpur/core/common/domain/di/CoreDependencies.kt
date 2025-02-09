package io.github.iamfacetheflames.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.common.domain.Logger
import io.github.iamfacetheflames.rangpur.core.common.domain.model.CoreConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.interactor.LibraryInteractor
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
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
    initLibrary(this, database)
}

private fun initLibrary(
    di: DependencyInjector,
    database: Database,
) = di.apply {
    addSingleton(
        LibraryRepository::class,
        database.library,
    )
    add(LibraryInteractor::class) {
        LibraryInteractor(di)
    }
}