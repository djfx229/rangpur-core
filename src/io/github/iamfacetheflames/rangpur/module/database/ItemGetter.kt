package io.github.iamfacetheflames.rangpur.module.database

import io.github.iamfacetheflames.rangpur.data.WithId

interface ItemGetter<T : WithId> {
    fun getItem(uuid: String): T
}