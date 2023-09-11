package dev.efnilite.ztd.tower

import dev.efnilite.ztd.troop.Troop2

enum class TargetMode(val sort: (List<Troop2>) -> List<Troop2>) {

    FIRST({ list -> list }),
    LAST({ list -> list.reversed() }),
    STRONG({ list -> list.sortedBy { troop -> troop.health }}),
    WEAK({ list -> list.sortedBy { troop -> troop.health }.reversed() });

}