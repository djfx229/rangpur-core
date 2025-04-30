package io.github.djfx229.rangpur.core.common.domain.di

import io.github.iamfacetheflames.rangpur.core.feature.player.domain.controller.PlayerController
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.interactor.PlayerInteractor
import io.github.iamfacetheflames.rangpur.core.common.domain.Logger
import io.github.iamfacetheflames.rangpur.core.common.domain.interactor.DirectoryInteractor
import io.github.iamfacetheflames.rangpur.core.common.domain.model.ApplicationConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.model.CoreConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.interactor.LibraryInteractor
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
import io.github.iamfacetheflames.rangpur.core.feature.player.data.repository.PlayerConfigRepository
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerConfig
import io.github.iamfacetheflames.rangpur.core.repository.database.Database

/**
 * Метод, осуществляющий регистрацию зависимостей необходимых для классов из core.
 *
 * Создан в связи с необходимостью иметь возможность в runtime подсветить отсутствующие зависимости из core.
 */
fun DependencyInjector.registryCoreDependencies(
    applicationConfig: ApplicationConfig,
    logger: Logger,
    database: Database,
    configRepository: ConfigRepository<CoreConfig>,
    playerController: PlayerController,
) {
    addSingleton(
        ApplicationConfig::class,
        applicationConfig,
    )
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
    addSingleton(
        DirectoryInteractor::class,
        DirectoryInteractor(this)
    )
    initLibrary(this, database)
    initPlayer(this, playerController)
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

private fun initPlayer(
    di: DependencyInjector,
    playerController: PlayerController,
) = di.apply {
    addSingleton(
        ConfigRepository::class,
        dependencyName = PlayerConfig::class.qualifiedName,
        dependency = PlayerConfigRepository(
            logger = di.get(),
            applicationConfig = di.get()
        )
    )
    addSingleton(
        PlayerController::class,
        playerController,
    )
    add(PlayerInteractor::class) {
        PlayerInteractor(di)
    }
}
