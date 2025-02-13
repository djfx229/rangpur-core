package io.github.iamfacetheflames.rangpur.core.feature.library.data.repository

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.core.common.data.database.SqliteRequestUtils
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.AudioField
import io.github.iamfacetheflames.rangpur.core.data.SortDirection
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.*
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
import io.github.iamfacetheflames.rangpur.ormlite.data.OrmLiteAudio
import java.lang.StringBuilder

class LibraryRepositoryImpl(
    private var source: ConnectionSource,
): LibraryRepository {
    override fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)

        // Значения, которые нуждаются в экранировании (защите от sql инъекции), собираются здесь, а вместо них в
        // request проставляются вопросительные знаки. Метод dao.queryRaw() умеет принимать vararg из значений, которые
        // заменяют вопросительные знаки на нужные значения.
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
                    is FilterItem.KeyList -> mapKeyListItemToCondition(item)
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

    private fun mapKeyListItemToCondition(item: FilterItem.KeyList): String {
        return SqliteRequestUtils.inArray(FilteredAudioField.KEY.toDatabaseField(), item.keys.map { it.sortPosition.toString() })
    }

    private fun mapDateListItemToCondition(item: FilterItem.DateList): String {
        return SqliteRequestUtils.likeOrExpression(FilteredAudioField.DATE_CREATED.toDatabaseField(), item.dateList)
    }

    private fun mapDirectoriesItemToCondition(item: FilterItem.Directories): String {
        val locationDirs = mutableListOf<String>()
        item.directories.forEach {
            it.locationInMusicDirectory?.let(locationDirs::add)
        }
        return SqliteRequestUtils.likeOrExpression("p.location", locationDirs, true)
    }

    private fun mapNumericItemToCondition(item: FilterItem.Numeric): String {
        val fieldName = item.field.toDatabaseField()
        return if (item.isRange) {
            " $fieldName BETWEEN ${item.min} AND ${item.max} "
        } else {
            " $fieldName = ${item.value} "
        }
    }

    private fun mapTextItemToCondition(item: FilterItem.Text, args: MutableList<String>): String {
        args.add("%${item.value}%")
        val fieldName = item.field.toDatabaseField()
        return " $fieldName LIKE ? "
    }

    private fun FilteredAudioField.toDatabaseField(): String {
        return when (this) {
            FilteredAudioField.DATE_CREATED -> AudioField.DATE_CREATED
            FilteredAudioField.ARTIST -> AudioField.ARTIST
            FilteredAudioField.TITLE -> AudioField.TITLE
            FilteredAudioField.ALBUM -> AudioField.ALBUM
            FilteredAudioField.COMMENT -> AudioField.COMMENT
            FilteredAudioField.FILE_NAME -> AudioField.FILE_NAME
            FilteredAudioField.BITRATE -> AudioField.BITRATE
            FilteredAudioField.KEY -> AudioField.KEY_SORT_POSITION
            FilteredAudioField.BPM -> AudioField.BPM
        }
    }

    private fun SortedAudioField.toDatabaseField(): String {
        return when (this) {
            SortedAudioField.KEY_SORT_POSITION -> AudioField.KEY_SORT_POSITION
            SortedAudioField.TIMESTAMP_CREATED -> AudioField.TIMESTAMP_CREATED
        }
    }
}
