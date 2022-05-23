package io.github.iamfacetheflames.rangpur.core.repository.database

import io.github.iamfacetheflames.rangpur.core.data.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}