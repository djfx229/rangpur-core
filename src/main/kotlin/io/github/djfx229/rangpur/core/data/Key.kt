package io.github.djfx229.rangpur.core.data

object Keys {

    data class Key (
        val lancelot: String,
        val traditional: String,
        val sortPosition: Int
    ) {
        override fun toString(): String = lancelot
    }

    /**
     * Странный порядок достался нам в наследство от Mixxx
     *
     * Lancelot Wheel:
     * A - минорные
     * B - мажорные
     *
     * sortPosition располагает тональности в следущем порядке: 1А, 1B, 2A, 2B, ... 12A, 12B.
     * Отсчёт sortPosition начинается с 1.
     */
    val keyMap: Map<Int, Key> = mapOf(
        0 to emptyValue(),
        1 to Key ("8B",      "C",      16),
        2 to Key ("3B",      "D♭",      6),
        3 to Key ("10B",     "D",      20),
        4 to Key ("5B",      "E♭",     10),
        5 to Key ("12B",     "E",      24),
        6 to Key ("7B",      "F",      14),
        7 to Key ("2B",      "F♯/G♭",   4),
        8 to Key ("9B",      "G",      18),
        9 to Key ("4B",      "A♭",      8),
        10 to Key("11B",     "A",      22),
        11 to Key("6B",      "B♭",     12),
        12 to Key("1B",      "B",       2),
        13 to Key("5A",      "Cm",      9),
        14 to Key("12A",     "C♯m",    23),
        15 to Key("7A",      "Dm",     13),
        16 to Key("2A",      "D♯m/E♭m", 3),
        17 to Key("9A",      "Em",     17),
        18 to Key("4A",      "Fm",      7),
        19 to Key("11A",     "F♯m",    21),
        20 to Key("6A",      "Gm",     11),
        21 to Key("1A",      "G♯m",     1),
        22 to Key("8A",      "Am",     15),
        23 to Key("3A",      "B♭m",     5),
        24 to Key("10A",     "Bm",     19)
    )

    val lancelotMap: Map<String, Key> by lazy {
        val resultMap = mutableMapOf<String, Key>()
        keyMap.forEach { _, value ->
            resultMap[value.lancelot] = value
        }
        resultMap
    }

    val positionMap: Map<Int, Key> by lazy {
        val resultMap = mutableMapOf<Int, Key>()
        keyMap.forEach { _, value ->
            resultMap[value.sortPosition] = value
        }
        resultMap
    }

    /**
     * В данный момент берём лишь 4ре варианта:
     * - текущую
     * - параллельную тональность
     * - на кварту вниз
     * - на квинту вверх
     *
     * https://habr.com/ru/articles/59261/comments/#comment_1606189
     * в будущем можно будет попробовать и другие способы гармонического сведения — например, в одноименную
     * тональность, через гармонические обороты типа 2-5-1, 1-6-2 или вообще по хроматической гамме.
     */
    fun Key.plusAllCompatible(): List<Key> {
        val parallelKey = if (isMinor()) {
            sortPositionPlus(1)
        } else {
            sortPositionMinus(1)
        }
        val quartDownKey = lancelotMinus(1)
        val fifthUpKey = lancelotPlus(1)

        return listOf(
            this,
            parallelKey,
            quartDownKey,
            fifthUpKey,
        )
    }

    fun Key.sortPositionPlus(value: Int): Key {
        val result = sortPosition + value
        val position = if (result > 24) {
            result - 24
        } else {
            result
        }
        return positionMap[position]!!
    }

    fun Key.sortPositionMinus(value: Int): Key {
        val result = sortPosition - value
        val position = if (result < 1) {
            result + 24
        } else {
            result
        }
        return positionMap[position]!!
    }

    fun Key.lancelotPlus(value: Int): Key = sortPositionPlus(value * 2)

    fun Key.lancelotMinus(value: Int): Key = sortPositionMinus(value * 2)

    fun Key.isMinor(): Boolean = sortPosition % 2 != 0

    fun Key.isMajor(): Boolean = !isMinor()

    fun get(index: Int) = keyMap.getOrDefault(index, emptyValue())

    fun emptyValue() = Key("", "", 0)

}