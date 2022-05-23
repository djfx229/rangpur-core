package io.github.iamfacetheflames.rangpur.repository.database

import io.github.iamfacetheflames.rangpur.data.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}