package io.github.iamfacetheflames.rangpur.core.repository.database

import io.github.iamfacetheflames.rangpur.core.data.WithId

interface ItemGetter<T : WithId> {
    fun getItem(uuid: String): T
}