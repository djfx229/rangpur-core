package io.github.iamfacetheflames.rangpur.data

interface Directory: WithId {
    var parent: Directory?
    var locationInMusicDirectory: String?
    var name: String?
}

