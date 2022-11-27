package io.github.iamfacetheflames.rangpur.core.data

import java.io.Serializable
import java.util.*

interface WithId : Serializable {
    var uuid: String
}

fun WithId.equalsUUID(other: Any?): Boolean {
    return if (other is WithId) {
        other.uuid == this.uuid
    } else {
        false
    }
}

abstract class TestItem : WithId {
    companion object {
        @JvmStatic
        fun generateUuid(): String = UUID.randomUUID().toString()
    }
}