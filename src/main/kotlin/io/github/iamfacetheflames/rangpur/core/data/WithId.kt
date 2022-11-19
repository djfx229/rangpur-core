package io.github.iamfacetheflames.rangpur.core.data

import java.io.Serializable

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