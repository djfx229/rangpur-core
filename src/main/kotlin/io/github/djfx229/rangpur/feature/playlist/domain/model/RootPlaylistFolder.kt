package io.github.djfx229.rangpur.feature.playlist.domain.model

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