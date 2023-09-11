package dev.efnilite.ztd.tower

import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.tower.util.TroopList
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue

class Mage(center: Location, blocks: Set<Block>, owner: dev.efnilite.ztd.TowerPlayer, config: dev.efnilite.ztd.Config) : Tower(
    center, blocks, owner,
    config
) {

    private var fireLength: Int = 20
    private var damageBonus: Int = 0
    private var targetCount: Int = 1

    override fun construct() {
        super.construct()

        fireLength = getSpecial("fire_length") as Int
        damageBonus = getSpecial("damage_bonus") as Int
        targetCount = getSpecial("target_count") as Int
    }

    override fun upgrade() {
        fireLength = getSpecial("fire_length") as Int
        damageBonus = getSpecial("damage_bonus") as Int
        targetCount = getSpecial("target_count") as Int
    }

    override fun shoot(troopsInRange: TroopList) {
        for (idx in 0 until targetCount) {
            val target = troopsInRange.get(idx)

            target.entity.fireTicks = fireLength
            target.entity.setMetadata("ztd fire", FixedMetadataValue(
                ZTD.instance,
                FireData(this, getDamage(), damageBonus)
            ))
        }
    }

    data class FireData(val owner: Tower, val damage: Int, val damageBonus: Int)

    override fun getName(): String = "mage"
}