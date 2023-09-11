package dev.efnilite.ztd.tower.util

import dev.efnilite.ztd.troop.Troop2
import java.util.concurrent.ThreadLocalRandom

data class TroopList(val troops: List<Troop2>) : Iterable<Troop2> {

    fun isEmpty(): Boolean = troops.isEmpty()

    fun get(idx: Int): Troop2 =
        if (idx < troops.size) troops[idx]
        else troops[ThreadLocalRandom.current().nextInt(troops.size)]

    override fun iterator(): Iterator<Troop2> {
        return troops.iterator()
    }
}
