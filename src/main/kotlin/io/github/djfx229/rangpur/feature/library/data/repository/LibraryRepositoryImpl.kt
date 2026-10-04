package io.github.djfx229.rangpur.feature.library.data.repository

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.DatabaseTable
import io.github.djfx229.rangpur.common.data.database.*
import io.github.djfx229.rangpur.common.domain.model.sort.Sort
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.repository.LibraryRepository
import io.github.djfx229.rangpur.feature.library.data.entity.OrmLiteAudio
import java.sql.SQLException
import java.util.*

class LibraryRepositoryImpl(
    private var source: ConnectionSource,
): LibraryRepository {
    companion object {
        const val INNER_DIRECTORY = "dir"
    }

    override fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        return getAudios(filter, SqliteRequestUtils.sortedBy(sort))
    }

    /**
     * Игнорирует аудиозаписи, которые были отмечены как полученные в прошлом запросе с помощью [alreadyRequestedId].
     */
    override fun getRandomAudios(alreadyRequestedId: String, filter: Filter, limit: Int): List<Audio> {
        return getAudios(filter, "ORDER BY random() LIMIT $limit", alreadyRequestedId)
    }

    override fun markAsRequested(audios: List<Audio>, requestId: String) {
        val dao = DaoManager.createDao(source, OrmLiteRequestedAudio::class.java)
        dao.callBatchTasks {
            val request = "INSERT INTO requested_audios (uuid, audio_uuid, request_id) VALUES (?, ?, ?);"
            audios.forEach { audio ->
                try {
                    dao.executeRaw(request, UUID.randomUUID().toString(), audio.uuid, requestId)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // нарушение ограничения здесь не критично
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun clearRequestStatusMarkers() {
        val daoAudioInPlaylist = DaoManager.createDao(source, OrmLiteRequestedAudio::class.java)
        val request = "DELETE FROM requested_audios;"
        daoAudioInPlaylist.executeRaw(request)
    }

    private fun getAudios(
        filter: Filter,
        requestSubstring: String,
        alreadyRequestedId: String? = null,
    ): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)

        // Значения, которые нуждаются в экранировании (защите от sql инъекции), собираются здесь, а вместо них в
        // request проставляются вопросительные знаки. Метод dao.queryRaw() умеет принимать vararg из значений, которые
        // заменяют вопросительные знаки на нужные значения.
        val args = mutableListOf<String>()

        // собираем sql запрос
        val request = StringBuilder().apply {
            val playlistsFilter: FilterItem.Playlists? = filter.items.findLast {
                it is FilterItem.Playlists
            } as? FilterItem.Playlists
            val audioForSelect =  if (playlistsFilter != null && playlistsFilter.uuidItems.isNotEmpty()) {
                val values = playlistsFilter.uuidItems.map { "'$it'" }
                val inCondition = SqliteRequestUtils.inArray("aip.playlist_uuid ", values)
                if (playlistsFilter.isNot) {
                    """
                        SELECT
                            aa.* FROM audio as aa
                        LEFT JOIN 
                            audio_in_playlist as aip 
                            ON
                                aip.audio_uuid = aa.uuid 
                                AND $inCondition
                        WHERE aip.uuid IS NULL
                    """.trimIndent()
                } else {
                    """
                        SELECT
                            aa.* FROM audio as aa
                        INNER JOIN 
                            audio_in_playlist as aip 
                            ON
                                aip.audio_uuid = aa.uuid 
                                AND $inCondition
                        GROUP BY aa.uuid
                    """.trimIndent()
                }
            } else {
                "audio"
            }

            // основной select, к которому будут применятся where и сортировка
            append(
                "SELECT a.* FROM ($audioForSelect) as a " +
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
                        
                        if (alreadyRequestedId != null) {
                            val value = """
                            NOT EXISTS (
                                SELECT 1
                                FROM requested_audios AS req
                                WHERE req.audio_uuid = a.uuid AND req.request_id = '$alreadyRequestedId'
                            )
                            """.trimIndent()
                            add(SqlCondition(ConditionType.AND, value))
                        }
                    }
                )
            )

            append(requestSubstring)
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
                    val subConditions = item.items.mapNotNull {
                        mapItemToCondition(it, args, ConditionType.OR)
                    }
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
            is FilterItem.DateRange -> mapDateRangeItemToCondition(item)

            // Данные фильтры в рамках where применять не эффективно, они будут выполнены раньше.
            is FilterItem.Playlists -> ""
        }
        return if (value.isNotBlank()) {
            SqlCondition(
                type = type,
                value = value,
                isNot = item.isNot,
            )
        } else {
            null
        }
    }

    private fun mapDateRangeItemToCondition(item: FilterItem.DateRange): String {
        return " ${AudioField.DATE_CREATED} BETWEEN ${item.min} AND ${item.max} "
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
            FilteredAudioField.DURATION -> AudioField.DURATION
            FilteredAudioField.PLAYLISTS -> throw IllegalStateException()
        }
    }

}

@DatabaseTable(tableName = "requested_audios")
class OrmLiteRequestedAudio {

    @DatabaseField(
        columnName = "uuid",
        id = true,
        canBeNull = false,
        uniqueIndexName = "unique_uuid",
    )
    var uuid: String = UUID.randomUUID().toString()

    @DatabaseField(
        columnName = "audio_uuid",
        foreign = true,
        foreignAutoCreate = true,
        foreignAutoRefresh = true,
        uniqueCombo = true,
        canBeNull = false,
    )
    var ormAudio: OrmLiteAudio? = null

    @DatabaseField(
        columnName = "request_id",
        canBeNull = false,
        uniqueCombo = true,
    )
    var requestId: String? = null

}
