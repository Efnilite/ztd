package dev.efnilite.ztd.world

import dev.efnilite.ztd.session.Session
import org.bukkit.Location
import java.util.*
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 *
 * Divides the parkour world in sections, each with an active session.
 *
 * Iteration 2.
 *
 * @author Efnilite
 * @since 5.0.0
 */
object WorldDivider {

    private const val defaultY = 150.0
    private const val borderSize = 2000.0

    /**
     * Map with all session ids map to the session instances.
     */
    private val sessions: MutableMap<Int, Session> = HashMap()
    private val sessionIds: MutableMap<UUID, Session> = HashMap()

    fun getSessions(): MutableCollection<Session> = sessions.values

    /**
     * Associates a session to a specific section.
     *
     * @param session The session.
     */
    @Synchronized
    fun associate(session: Session) {
        // attempts to get the closest available section to the center
        var n = 0
        while (sessions.containsKey(n)) {
            n++
        }
        sessions[n] = session
        sessionIds[session.uuid] = session
    }

    /**
     * Disassociates a session from a specific section.
     *
     * @param session The session.
     */
    fun disassociate(session: Session) {
        sessions.remove(getSectionId(session))
        sessionIds.remove(session.uuid)
    }

    /**
     * @param session The session.
     * @return The location at the center of section n.
     */
    fun toLocation(session: Session): Location {
        val xz = WorldDivider.spiralAt(getSectionId(session))

        return Location(
            dev.efnilite.ztd.ZTD.world,
            xz[0] * borderSize,
            defaultY,
            xz[1] * borderSize
        )
    }

    // returns the section id from the session instance. error if no found.
    private fun getSectionId(session: Session): Int {
        return sessions.entries
            .filter { entry -> entry.value == session }
            .map { entry -> entry.key }
            .first()
    }

    /**
     * @param session The session.
     * @return Array where the first item is the smallest location and second item is the largest.
     */
    fun toSelection(session: Session): Array<Location> {
        val center = toLocation(session)

        // get the min and max locations
        val max = center.clone().add(
            borderSize / 2,
            defaultY, borderSize / 2)
        val min = center.clone().subtract(
            borderSize / 2,
            defaultY, borderSize / 2)
        return arrayOf(min, max)
    }

    /**
     * Returns a session.
     */
    fun getSession(uuid: UUID): Session? {
        return sessionIds[uuid]
    }

    /**
     * Gets a spiral
     *
     * @param nth The number of  value
     * @return the coords of this value
     */
    // https://math.stackexchange.com/a/163101
    private fun spiralAt(nth: Int): IntArray {

        var n = nth
        require(n >= 0) { "Invalid n bound: $n" }
        n++ // one-index
        val k = ceil((sqrt(n.toDouble()) - 1) / 2).toInt()
        var t = 2 * k + 1
        var m = t * t
        t--
        m -= if (n > m - t) {
            return intArrayOf(k - (m - n), -k)
        } else {
            t
        }
        m -= if (n > m - t) {
            return intArrayOf(-k, -k + (m - n))
        } else {
            t
        }
        return if (n > m - t) {
            intArrayOf(-k + (m - n), k)
        } else {
            intArrayOf(k, k - (m - n - t))
        }
    }
}