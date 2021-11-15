package io.github.iamfacetheflames.rangpur.data

enum class Direction {
    DESC, ASC
}
sealed class Sort(val columnName: String, var direction: Direction) {
    val directionName: String
        get() = direction.name

    override fun toString(): String = columnName
}
class DefaultSort(direction: Direction = Direction.DESC): Sort("default", direction)
class DateSort(direction: Direction = Direction.DESC): Sort(AudioField.TIMESTAMP_CREATED, direction)
class KeySort(direction: Direction = Direction.DESC): Sort(AudioField.KEY_SORT_POSITION, direction)

fun getSorts() = arrayOf<Sort>(
    DefaultSort(),
    DateSort(),
    KeySort()
)