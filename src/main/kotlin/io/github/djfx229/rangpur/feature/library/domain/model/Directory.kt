package io.github.djfx229.rangpur.feature.library.domain.model

import io.github.djfx229.rangpur.common.domain.model.TestItem
import io.github.djfx229.rangpur.common.domain.model.WithId

interface Directory : WithId {
    var parent: Directory?

    /**
     * Относительный путь до директории (внутри директории фонотеки)
     *
     * Должен начинаться со слэша.
      */
    var locationInMusicDirectory: String?

    var name: String?
}

class TestDirectory(
    override var name: String?,
    override var locationInMusicDirectory: String?,
    override var parent: Directory? = null,
    override var uuid: String = generateUuid(),
) : TestItem(), Directory

const val universalSeparator = "/"
