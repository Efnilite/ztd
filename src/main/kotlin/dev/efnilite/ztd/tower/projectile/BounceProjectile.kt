package dev.efnilite.ztd.tower.projectile

import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.troop.Troop2

open class BounceProjectile(
    target: Troop2, owner: Tower, speed: Double, damage: Int,
    val bounceCount: Int, val bounceRadius: Double,
) : Projectile(target, owner, speed, damage) {

    open fun getNewInstance(newTarget: Troop2) = this

}