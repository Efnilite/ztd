package dev.efnilite.ztd.world

import dev.efnilite.ztd.session.Session
import org.bukkit.Location
import org.jetbrains.annotations.Contract
import kotlin.math.ceil
import kotlin.math.sqrt


/**
 * Divides the world into sections.
 */
object Divider {

    private val sections = mutableMapOf<Session, Int>()

    val sessions: Set<Session>
        get() = sections.keys

    /**
     * Adds a session to the divider.
     * @param session The session to add.
     * @return The section the session was added to.
     */
    fun add(session: Session): Int {
        val missing = (0..sections.size)
            .first { !sections.values.contains(it) }

        sections[session] = missing

        return missing
    }

    /**
     * Returns the center location of the session.
     * @param session The session to get the location of.
     * @return The center location of the session.
     */
    fun toLocation(session: Session): Location {
        val idx = sections[session]!!

        val head = spiralAt(idx)

        val x = head.first
        val y = 150.0
        val z = head.second

        return Location(ZWorld.world, x * 10000.0, y, z * 10000.0)
    }

    /**
     * Removes a session from the divider.
     * @param session The session to remove.
     */
    fun remove(session: Session) {
        sections.remove(session)
    }

    /**
     * Clears the divider.
     */
    fun clear() {
        sections.clear()
    }

    // todo remove magic code
    // https://math.stackexchange.com/a/163101
    @Contract(pure = true)
    private fun spiralAt(n: Int): Pair<Int, Int> {
        require(n >= 0) { "Invalid n bound: $n" }

        var n = n
        n++ // one-index
        val k = ceil((sqrt(n.toDouble()) - 1) / 2).toInt()
        var t = 2 * k + 1
        var m = t * t

        t--
        m -= if (n > m - t) {
            return k - (m - n) to -k
        } else {
            t
        }

        m -= if (n > m - t) {
            return -k to -k + (m - n)
        } else {
            t
        }

        return if (n > m - t) {
            -k + (m - n) to k
        } else {
            k to k - (m - n - t)
        }
    }
}