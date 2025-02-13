package io.github.iamfacetheflames.rangpur.core.common.data.database

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

}