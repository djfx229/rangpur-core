package io.github.djfx229.rangpur.core.repository.database

import io.github.djfx229.rangpur.feature.database.domain.model.WithId

interface ItemGetter<T : WithId> {
    fun getItem(uuid: String): T
}