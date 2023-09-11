package dev.efnilite.ztd.tower.projectile

import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.troop.Troop2

class FinalBounceProjectile(
    target: Troop2, owner: Tower, speed: Double, damage: Int, bounceCount: Int, bounceRadius: Double
) : BounceProjectile(target, owner, speed, damage, bounceCount, bounceRadius) {

    override fun getNewInstance(newTarget: Troop2): BounceProjectile =
        FinalBounceProjectile(newTarget, owner, speed, damage, bounceCount - 1, bounceRadius)

    override fun getProjectileDamage(): Int = if (bounceCount == 0) damage + 1 else damage

}