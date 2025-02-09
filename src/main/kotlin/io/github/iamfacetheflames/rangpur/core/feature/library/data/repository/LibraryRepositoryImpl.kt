package io.github.iamfacetheflames.rangpur.core.feature.library.data.repository

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.FilteredAudioField
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.Filter
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.FilterItem
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
import io.github.iamfacetheflames.rangpur.ormlite.data.OrmLiteAudio
import io.github.iamfacetheflames.rangpur.ormlite.repository.database.likeOrExpression
import java.lang.StringBuilder

class LibraryRepositoryImpl(
    private var source: ConnectionSource,
): LibraryRepository {
    override fun getAudios(filter: Filter): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)

        // Значения, которые нуждаются в экранировании (защите от sql инъекции) собираются здесь, а
        // вместо них в request проставляются ?
        val args = mutableListOf<String>()

        // собираем sql запрос на основе входного фильтра
        val request = StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN directory as p ON a.directory_uuid = p.uuid ")
            filter.items.forEachIndexed { index, item ->
                if (index == 0 && filter.items.isNotEmpty()) {
                    append("WHERE ")
                }
                if (index > 0) {
                    append("AND ")
                }
                val condition = when (item) {
                    is FilterItem.Text -> convertTextItemToCondition(item, args)
                    is FilterItem.Numeric -> convertNumericItemToCondition(item)
                    is FilterItem.Directories -> convertDirectoriesItemToCondition(item)
                    is FilterItem.DateList -> convertDateListItemToCondition(item)
                }
                append(condition)
            }
        }.toString()

        return dao.queryRaw(request, dao.rawRowMapper, *args.toTypedArray()).results
    }

    private fun convertDateListItemToCondition(item: FilterItem.DateList): String {
        return likeOrExpression(FilteredAudioField.dateCreated.toDatabaseField(), item.dateList)
    }

    private fun convertDirectoriesItemToCondition(item: FilterItem.Directories): String {
        val locationDirs = mutableListOf<String>()
        item.directories.forEach {
            it.locationInMusicDirectory?.let(locationDirs::add)
        }
        return likeOrExpression("p.location", locationDirs, true)
    }

    private fun convertNumericItemToCondition(item: FilterItem.Numeric): String {
        val fieldName = item.field.toDatabaseField()
        return " $fieldName = ${item.value} "
    }

    private fun convertTextItemToCondition(item: FilterItem.Text, args: MutableList<String>): String {
        args.add("%${item.value}%")
        val fieldName = item.field.toDatabaseField()
        return " $fieldName LIKE ? "
    }

    private fun FilteredAudioField.toDatabaseField(): String {
        return when (this) {
            FilteredAudioField.comment -> "comment"
            FilteredAudioField.bpm -> "bpm"
            FilteredAudioField.dateCreated -> "date_created"
            FilteredAudioField.fileName -> "file_name"
        }
    }
}
