package io.github.iamfacetheflames.rangpur.data

import java.io.Serializable

interface WithId : Serializable {
    var id: Long // legacyString
    var uuid: String
}

fun WithId.equalsUUID(other: Any?): Boolean {
    return if (other is WithId) {
        other.uuid == this.uuid
    } else {
        false
    }
}