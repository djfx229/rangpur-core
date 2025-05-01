package io.github.djfx229.rangpur.common.domain.database

import io.github.djfx229.rangpur.common.domain.model.WithId

interface ItemGetter<T : WithId> {
    fun getItem(uuid: String): T
}