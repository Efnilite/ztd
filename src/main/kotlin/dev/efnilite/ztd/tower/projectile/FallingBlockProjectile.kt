package dev.efnilite.ztd.tower.projectile

import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.troop.Troop2

class FallingBlockProjectile(
    target: Troop2, owner: Tower, speed: Double, damage: Int,
    val fallCount: Int, val fallRadius: Double, val knockback: Int,
    val slow: Int
) : Projectile(
    target, owner, speed, damage
)