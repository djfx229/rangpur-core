package io.github.djfx229.rangpur.feature.library.domain.repository

import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.filter.Filter
import io.github.djfx229.rangpur.core.common.domain.model.sort.Sort

interface LibraryRepository {
    fun getAudios(filter: Filter, sort: Sort): List<Audio>
}
