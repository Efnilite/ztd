package dev.efnilite.ztd.troop

import dev.efnilite.ztd.session.Team
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.*

class Path(pos: List<Location>) {

    private val positions: Queue<Location> = LinkedList(pos)

    fun getTarget(): Location = positions.peek()

    /**
     * Returns the current target. If [troop] is close enough to the current target,
     * the target will be updated.
     */
    private fun getTarget(troop: Troop2): Location {
        val target = positions.peek()

        val nullifiedY = troop.location.clone()
        nullifiedY.y = target.y

        // todo cache locations

        if (DISTANCE_THRESHOLD * DISTANCE_THRESHOLD > target.distanceSquared(nullifiedY)) {
            if (positions.size > 1) {
                return positions.poll()
            }

            // reached final point
            val opposite = Team.getOpposite(troop.owner.team)
            troop.session.damageTeam(opposite, TroopType.getHealthFromTroop(troop.type))
            troop.entity.remove()

            return target
        }
        return target
    }

    /**
     * Returns the current heading towards the next target.
     * Returns a normalized vector with the heading, multiplied by a factor of [speed].
     */
    fun getHeading(troop: Troop2, speed: Double): Vector = getTarget(troop).clone()
        .subtract(troop.location.toVector())
        .toVector()
        .normalize()
        .multiply(speed)

    fun clone() = Path(positions.toList())

    companion object {
        private const val DISTANCE_THRESHOLD = 0.3
    }
}