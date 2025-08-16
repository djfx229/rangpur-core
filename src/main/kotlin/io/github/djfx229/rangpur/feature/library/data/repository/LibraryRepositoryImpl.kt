package io.github.djfx229.rangpur.feature.library.data.repository

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.common.data.database.SqliteRequestUtils
import io.github.djfx229.rangpur.common.domain.model.sort.Sort
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.common.data.database.AudioField
import io.github.djfx229.rangpur.common.data.database.ConditionType
import io.github.djfx229.rangpur.common.data.database.SqlCondition
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.repository.LibraryRepository
import io.github.djfx229.rangpur.feature.library.data.entity.OrmLiteAudio

class LibraryRepositoryImpl(
    private var source: ConnectionSource,
): LibraryRepository {
    companion object {
        const val INNER_DIRECTORY = "dir"
    }

    override fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)

        // Значения, которые нуждаются в экранировании (защите от sql инъекции), собираются здесь, а вместо них в
        // request проставляются вопросительные знаки. Метод dao.queryRaw() умеет принимать vararg из значений, которые
        // заменяют вопросительные знаки на нужные значения.
        val args = mutableListOf<String>()

        // собираем sql запрос
        val request = StringBuilder().apply {
            append(
                "SELECT a.* FROM audio as a " +
                        "INNER JOIN directory as $INNER_DIRECTORY " +
                        "ON a.directory_uuid = $INNER_DIRECTORY.uuid "
            )

            // применяем условия фильтра
            append(
                SqliteRequestUtils.where(
                    buildList {
                        filter.items.forEach { item ->
                            val condition = mapItemToCondition(item, args)
                            if (condition != null) {
                                add(condition)
                            }
                        }
                    }
                )
            )

            append(SqliteRequestUtils.sortedBy(sort))
            append(";")
        }.toString()

        return dao.queryRaw(request, dao.rawRowMapper, *args.toTypedArray()).results
    }

    private fun mapItemToCondition(
        item: FilterItem,
        args: MutableList<String>,
        type: ConditionType = ConditionType.AND,
    ): SqlCondition? {
        val value = when (item) {
            is FilterItem.Text -> mapTextItemToCondition(item, args)
            is FilterItem.Numeric -> mapNumericItemToCondition(item)
            is FilterItem.TextSet -> mapTextSetItemToCondition(item)
            is FilterItem.KeyList -> mapKeyListItemToCondition(item)
            is FilterItem.OnlyWithoutPlaylists -> mapOnlyWithoutPlaylistsItemToCondition(item)
            is FilterItem.MultiplyItems -> {
                if (item.items.isNotEmpty()) {
                    val subConditions = item.items.map {
                        mapItemToCondition(it, args, ConditionType.OR)
                    }.filterNotNull()
                    val mergedConditions = SqliteRequestUtils.mergeConditions(subConditions)
                    if (mergedConditions.isNotBlank()) {
                        "($mergedConditions)"
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            }
        }
        return if (value.isNotBlank()) {
            SqlCondition(
                type = type,
                value = value,
            )
        } else {
            null
        }
    }

    private fun mapOnlyWithoutPlaylistsItemToCondition(item: FilterItem.OnlyWithoutPlaylists): String {
        return if (item.isOnlyWithoutPlaylist) {
            " (SELECT COUNT(*) FROM audio_in_playlist AS aip WHERE aip.audio_uuid = a.uuid) == 0 "
        } else {
            ""
        }
    }

    private fun mapKeyListItemToCondition(item: FilterItem.KeyList): String {
        return SqliteRequestUtils.inArray(FilteredAudioField.KEY.toDatabaseField(), item.keys.map { it.sortPosition.toString() })
    }

    private fun mapTextSetItemToCondition(item: FilterItem.TextSet): String {
        return if (item.values.isNotEmpty()) {
            val items = buildList {
                item.values.forEach { add(it) }
            }
            SqliteRequestUtils.likeOrExpression(item.field.toDatabaseField(), items, true)
        } else {
            ""
        }
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
            FilteredAudioField.DIRECTORY_LOCATION -> "$INNER_DIRECTORY.location"
        }
    }

}
