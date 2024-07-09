package io.github.iamfacetheflames.rangpur.core.common.domain.model

/***
 * Конфигурация приложения с полями которые определяются ещё до рантайма и все значения являются статическими.
 *
 * Подразумевается что для модели, реализующей данный интерфейс, репозиторий не требуется, их можно сразу зарегать в di.
 */
interface ApplicationConfig : Config {

    val version: String

    /**
     * Путь до директории с хранилищем данных приложения (конфигами и базой данных).
     * Не путать с директорией, где хранится фонотека, её путь определяется другим конфигом [CoreConfig.musicLibraryPath]
     *
     * Без слэша на конце.
     */
    val appStoragePath: String

    val appTitle: String
        get() = "Rangpur $version"

    /**
     * Путь до директории с конфигами rangpur внутри [appStoragePath].
     *
     * Без слэша на конце.
     */
    val configsPath: String
        get() = "$appStoragePath/configs"

}