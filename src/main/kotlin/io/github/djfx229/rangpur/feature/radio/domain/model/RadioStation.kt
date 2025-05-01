package io.github.djfx229.rangpur.feature.radio.domain.model

import io.github.djfx229.rangpur.common.domain.model.WithId
import java.util.UUID

interface RadioStation : WithId {
    var name: String
    var description: String
    var streamUrl: String
}

class RadioStationStab(
    override var uuid: String = UUID.randomUUID().toString(),
    override var name: String,
    override var description: String,
    override var streamUrl: String
) : RadioStation
