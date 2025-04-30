package io.github.djfx229.rangpur.core.repository.database

import io.github.djfx229.rangpur.feature.database.domain.model.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}