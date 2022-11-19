package io.github.iamfacetheflames.rangpur.core.data

object Keys {

    class Key (
        val lancelot: String,
        val traditional: String,
        val sortPosition: Int
    ) {
        override fun toString(): String = lancelot
    }

    val keyMap = mutableMapOf(
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

    fun get(index: Int) = keyMap.getOrDefault(index, emptyValue())
    fun emptyValue() = Key("", "", 0)

}