package io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository

import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.Filter

interface LibraryRepository {
    fun getAudios(filter: Filter): List<Audio>
}
