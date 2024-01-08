package dev.efnilite.ztd.tower.util

import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.tower.projectile.Projectile
import dev.efnilite.ztd.troop.Troop2
import org.bukkit.Location
import org.bukkit.metadata.FixedMetadataValue

object TowerUtil {

    fun shootArrow(from: Location, troop: Troop2, tower: Tower, damage: Int = 1) {
        shootArrow(from, troop, Projectile(troop, tower, 3.0, damage))
    }

    fun shootArrow(from: Location, troop: Troop2, projectile: Projectile, speed: Double = 3.0) {
        val arrow = from.world.spawnArrow(from, troop.getCenter()
            .subtract(from)
            .toVector(), speed.toFloat(), 0.0F)

//        troop.targetedCount++
        arrow.setMetadata("ztd", FixedMetadataValue(ZTD.instance, projectile))
        arrow.isSilent = true
    }

}