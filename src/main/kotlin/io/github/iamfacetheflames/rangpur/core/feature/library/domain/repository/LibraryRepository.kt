package io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository

import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.Filter
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.Sort

interface LibraryRepository {
    fun getAudios(filter: Filter, sort: Sort): List<Audio>
}
