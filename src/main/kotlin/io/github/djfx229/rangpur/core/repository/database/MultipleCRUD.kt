package io.github.djfx229.rangpur.core.repository.database

import io.github.djfx229.rangpur.feature.database.domain.model.WithId

interface MultipleCRUD<T : WithId> : AllGetter<T> {
    fun create(items: List<T>)
    fun update(items: List<T>)
    fun delete(items: List<T>)
}