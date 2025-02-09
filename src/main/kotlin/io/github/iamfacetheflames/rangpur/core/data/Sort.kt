package io.github.iamfacetheflames.rangpur.core.data

enum class SortDirection {
    DESC, ASC
}
sealed class Sort(val columnName: String, var direction: SortDirection) {
    val directionName: String
        get() = direction.name

    override fun toString(): String = columnName
}
class DefaultSort(direction: SortDirection = SortDirection.DESC): Sort("default", direction)
class DateSort(direction: SortDirection = SortDirection.DESC): Sort(AudioField.TIMESTAMP_CREATED, direction)
class KeySort(direction: SortDirection = SortDirection.DESC): Sort(AudioField.KEY_SORT_POSITION, direction)

fun getSorts() = arrayOf<Sort>(
    DefaultSort(),
    DateSort(),
    KeySort()
)