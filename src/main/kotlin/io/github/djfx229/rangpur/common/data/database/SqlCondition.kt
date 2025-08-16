package io.github.djfx229.rangpur.common.data.database

enum class ConditionType {
    AND, OR
}

// Описывает условие внутри sql-запроса
data class SqlCondition(
    val type: ConditionType,
    val value: String,
    val isNot: Boolean = false,
)
