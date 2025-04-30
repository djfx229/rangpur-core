package io.github.djfx229.rangpur.core.common.data.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.djfx229.rangpur.core.common.domain.Logger
import io.github.djfx229.rangpur.core.common.domain.model.ApplicationConfig
import io.github.djfx229.rangpur.core.common.domain.model.Config
import io.github.djfx229.rangpur.core.common.domain.repository.ConfigRepository
import java.io.File
import java.io.FileNotFoundException
import java.lang.reflect.Type

/**
 * Реализация стандартного репозитория, использующего в качестве хранилища json файл.
 */
abstract class JsonConfigRepository<T : Config>(
    private val log: Logger,
    private val config: ApplicationConfig,
) : ConfigRepository<T>() {

    private fun absoluteConfigPath(): String = "${config.configsPath}/${jsonFilePath()}"

    private val file by lazy {
        File(absoluteConfigPath())
    }

    private var dto: T? = null

    /**
     * Относительный путь до данного конфига Т
     *
     * Путь указывается относительно [ApplicationConfig.appStoragePath]
     */
    abstract fun jsonFilePath(): String

    /**
     * Объект конфига для ситуации когда json файла не существует.
     */
    abstract fun defaultConfig(): T

    /**
     *  Полиморфизм не удался, из дженерика не получается достать Type typeOfT, поэтому его нужно указать явно,
     *  переопределив данный метод.
     */
    abstract fun configType(): Type

    override fun load() {
        dto = try {
            log.d(this, "путь до конфига: ${file.absolutePath}")
            if (file.exists()) {
                val raw = file.readText()
                Gson().fromJson(raw, configType())
            } else {
                defaultConfig()
            }
        } catch (e: FileNotFoundException) {
            // ignore
            defaultConfig()
        } catch (e: Exception) {
            log.e(this, e)
            defaultConfig()
        }
    }

    override fun get(): T = dto as T

    override fun save() {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(dto)
        file.createNewFile()
        file.writeText(json)
    }

}