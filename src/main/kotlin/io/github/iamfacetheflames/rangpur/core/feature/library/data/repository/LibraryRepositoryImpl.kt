package io.github.iamfacetheflames.rangpur.core.feature.library.data.repository

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.AudioField
import io.github.iamfacetheflames.rangpur.core.data.SortDirection
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.*
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
import io.github.iamfacetheflames.rangpur.ormlite.data.OrmLiteAudio
import io.github.iamfacetheflames.rangpur.ormlite.repository.database.likeOrExpression
import java.lang.StringBuilder

class LibraryRepositoryImpl(
    private var source: ConnectionSource,
): LibraryRepository {
    override fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)

        // Значения, которые нуждаются в экранировании (защите от sql инъекции) собираются здесь, а
        // вместо них в request проставляются ?
        val args = mutableListOf<String>()

        // собираем sql запрос
        val request = StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN directory as p ON a.directory_uuid = p.uuid ")

            // применяем условия фильтра
            filter.items.forEachIndexed { index, item ->
                if (index == 0 && filter.items.isNotEmpty()) {
                    append("WHERE ")
                }
                if (index > 0) {
                    append("AND ")
                }
                val condition = when (item) {
                    is FilterItem.Text -> mapTextItemToCondition(item, args)
                    is FilterItem.Numeric -> mapNumericItemToCondition(item)
                    is FilterItem.Directories -> mapDirectoriesItemToCondition(item)
                    is FilterItem.DateList -> mapDateListItemToCondition(item)
                }
                append(condition)
            }

            // применяем сортировку
            val sqlDirection = when (sort.direction) {
                SortDirection.DESC -> "DESC"
                SortDirection.ASC -> "ASC"
            }
            val fieldName = sort.field.toDatabaseField()
            append("ORDER BY $fieldName $sqlDirection ")
        }.toString()

        return dao.queryRaw(request, dao.rawRowMapper, *args.toTypedArray()).results
    }

    private fun mapDateListItemToCondition(item: FilterItem.DateList): String {
        return likeOrExpression(FilteredAudioField.DATE_CREATED.toDatabaseField(), item.dateList)
    }

    private fun mapDirectoriesItemToCondition(item: FilterItem.Directories): String {
        val locationDirs = mutableListOf<String>()
        item.directories.forEach {
            it.locationInMusicDirectory?.let(locationDirs::add)
        }
        return likeOrExpression("p.location", locationDirs, true)
    }

    private fun mapNumericItemToCondition(item: FilterItem.Numeric): String {
        val fieldName = item.field.toDatabaseField()
        return " $fieldName = ${item.value} "
    }

    private fun mapTextItemToCondition(item: FilterItem.Text, args: MutableList<String>): String {
        args.add("%${item.value}%")
        val fieldName = item.field.toDatabaseField()
        return " $fieldName LIKE ? "
    }

    private fun FilteredAudioField.toDatabaseField(): String {
        return when (this) {
            FilteredAudioField.COMMENT -> "comment"
            FilteredAudioField.BPM -> "bpm"
            FilteredAudioField.DATE_CREATED -> "date_created"
            FilteredAudioField.FILE_NAME -> "file_name"
        }
    }

    private fun SortedAudioField.toDatabaseField(): String {
        return when (this) {
            SortedAudioField.KEY_SORT_POSITION -> AudioField.KEY_SORT_POSITION
            SortedAudioField.TIMESTAMP_CREATED -> AudioField.TIMESTAMP_CREATED
        }
    }
}
