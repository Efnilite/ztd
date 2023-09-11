package dev.efnilite.ztd.tower.projectile

import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.troop.Troop2

open class Projectile(val target: Troop2, val owner: Tower, val speed: Double, protected val damage: Int) {

    open fun getProjectileDamage() = damage

}