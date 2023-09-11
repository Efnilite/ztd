package dev.efnilite.ztd.troop

import dev.efnilite.ztd.session.Team
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.*

class Path(pos: List<Location>) {

    private val positions: Queue<Location> = LinkedList()

    init {
        positions.addAll(pos)
    }

    fun getTarget(): Location = positions.peek()

    /**
     * Returns the current target. If [troop] is close enough to the current target,
     * the target will be updated.
     */
    fun getTarget(troop: Troop2): Location {
        val target: Location = positions.peek()

        val nullifiedY: Location = troop.location.clone()
        nullifiedY.y = target.y

        // todo cache locations

        if (distanceThreshold * distanceThreshold > target.distanceSquared(nullifiedY)) {
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
        private const val distanceThreshold = 0.3
    }
}