package io.github.iamfacetheflames.rangpur.data

import java.io.Serializable

interface Directory : WithId {
    var parent: Directory?
    var locationInMusicDirectory: String?
    var name: String?
}

