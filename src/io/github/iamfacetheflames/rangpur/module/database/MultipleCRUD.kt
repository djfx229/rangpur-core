package io.github.iamfacetheflames.rangpur.module.database

import io.github.iamfacetheflames.rangpur.data.WithId

interface MultipleCRUD<T : WithId> : AllGetter<T> {
    fun create(items: List<T>)
    fun update(items: List<T>)
    fun delete(items: List<T>)
}