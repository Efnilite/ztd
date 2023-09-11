package dev.efnilite.ztd.tower.projectile

import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.troop.Troop2

class IncrementalBounceProjectile(
    target: Troop2, owner: Tower, speed: Double, damage: Int, bounceCount: Int, bounceRadius: Double
) : BounceProjectile(target, owner, speed, damage, bounceCount, bounceRadius) {

    override fun getNewInstance(newTarget: Troop2): BounceProjectile =
        IncrementalBounceProjectile(
            newTarget,
            owner,
            speed,
            damage + 1,
            bounceCount - 1,
            bounceRadius
        )
}