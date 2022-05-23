package io.github.iamfacetheflames.rangpur.core.data

class RootPlaylistFolder(
    override var name: String? = "All",
    override var timestampCreated: Long = 0,
    override var parent: PlaylistFolder? = null,
    override var uuid: String = "All"
) : PlaylistFolder {

    override fun toString(): String {
        return name ?: super.toString()
    }

}