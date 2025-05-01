package io.github.djfx229.rangpur.common.domain.database

import io.github.djfx229.rangpur.common.domain.model.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}