package dev.efnilite.ztd.troop

import org.bukkit.util.Vector
import org.jetbrains.annotations.Contract
import kotlin.math.floor

/**
 * Represents a path for a troop to follow.
 * @param waypoints The waypoints as specified in the map file.
 * @param speed The distance between each step.t
 */
class Path2(val waypoints: List<Vector>, speed: Double) {

    /**
     * The points on the path.
     */
    val points = waypoints
        .zipWithNext { base, next -> interpolate(base, next, speed) }
        .flatten()

    /**
     * Interpolates movement between waypoints.
     */
    @Contract(pure = true)
    private fun interpolate(base: Vector, next: Vector, speed: Double): List<Vector> {
        val to = next.subtract(base)
        val steps = to.length() / speed
        val direction = to.normalize()

        return (0 until floor(steps).toInt())
            .map { base.clone().add(direction.multiply(steps * it)) }
    }
}