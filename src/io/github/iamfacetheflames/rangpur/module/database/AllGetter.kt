package io.github.iamfacetheflames.rangpur.module.database

import io.github.iamfacetheflames.rangpur.data.WithId

interface AllGetter<T : WithId> {
    fun getAll(): List<T>
}