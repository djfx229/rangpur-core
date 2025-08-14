package io.github.djfx229.rangpur.feature.library.domain.repository

import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.Filter
import io.github.djfx229.rangpur.common.domain.model.sort.Sort

interface LibraryRepository {
    fun getAudios(filter: Filter, sort: Sort): List<Audio>
}
