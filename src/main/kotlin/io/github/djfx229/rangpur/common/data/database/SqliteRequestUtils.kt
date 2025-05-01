package io.github.djfx229.rangpur.common.data.database

import io.github.djfx229.rangpur.common.domain.model.sort.Sort
import io.github.djfx229.rangpur.common.domain.model.sort.SortDirection
import io.github.djfx229.rangpur.common.domain.model.sort.SortedAudioField

object SqliteRequestUtils {

    fun where(conditions: List<String>): String =
        StringBuilder().apply {
            if (conditions.isNotEmpty()) {
                append("WHERE ")
                for ((index, item) in conditions.withIndex()) {
                    append(
                        if (index == 0) {
                            "$item "
                        } else {
                            "AND $item "
                        }
                    )
                }
            }
            append(" ")
        }.toString()

    fun likeOrExpression(field: String, values: List<String>, startsWith: Boolean = false): String =
        StringBuilder().apply {
            if (values.isNotEmpty()) {
                append("(")
                for ((index, item) in values.withIndex()) {
                    val value = like(field, item, startsWith)
                    append(
                        if (index == 0) {
                            "$value "
                        } else {
                            "OR $value "
                        }
                    )
                }
                append(")")
            }
            append(" ")
        }.toString()

    fun like(field: String, value: String, startsWith: Boolean = false): String {
        return if (startsWith) {
            "$field LIKE \"$value%\" "
        } else {
            "$field LIKE \"%$value%\" "
        }
    }

    fun inArray(field: String, array: List<String>): String {
        return if (array.isNotEmpty()) {
            val arrayString = StringBuilder().apply {
                append("(")
                for ((index, item) in array.withIndex()) {
                    val id = item
                    append(
                        if (index == 0) {
                            id
                        } else {
                            ", $id"
                        }
                    )
                }
                append(")")
            }.toString()
            "$field IN $arrayString "
        } else {
            " "
        }
    }

    fun sortedBy(sort: Sort): String {
        // применяем сортировку
        val sqlDirection = when (sort.direction) {
            SortDirection.DESC -> "DESC"
            SortDirection.ASC -> "ASC"
        }
        val fieldName = sort.field.toDatabaseField()
        return " ORDER BY $fieldName $sqlDirection "
    }

    private fun SortedAudioField.toDatabaseField(): String {
        return when (this) {
            SortedAudioField.KEY_SORT_POSITION -> AudioField.KEY_SORT_POSITION
            SortedAudioField.TIMESTAMP_CREATED -> AudioField.TIMESTAMP_CREATED
            SortedAudioField.ARTIST -> AudioField.ARTIST
            SortedAudioField.TITLE -> AudioField.TITLE
            SortedAudioField.ALBUM -> AudioField.ALBUM
            SortedAudioField.COMMENT -> AudioField.COMMENT
            SortedAudioField.FILE_NAME -> AudioField.FILE_NAME
            SortedAudioField.BITRATE -> AudioField.BITRATE
            SortedAudioField.KEY -> AudioField.KEY_SORT_POSITION
            SortedAudioField.BPM -> AudioField.BPM
        }
    }

}