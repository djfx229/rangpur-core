package io.github.iamfacetheflames.rangpur.data

enum class Direction {
    DESC, ASC
}
sealed class Sort(val columnName: String, var direction: Direction) {
    val directionName: String
        get() = direction.name

    override fun toString(): String = columnName
}
class DateSort(direction: Direction = Direction.DESC): Sort(AudioField.DATE_CREATED, direction)
class KeySort(direction: Direction = Direction.DESC): Sort(AudioField.KEY_SORT_POSITION, direction)