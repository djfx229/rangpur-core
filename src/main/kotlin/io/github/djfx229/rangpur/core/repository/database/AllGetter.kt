package io.github.djfx229.rangpur.core.repository.database

import io.github.djfx229.rangpur.core.data.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}