package dev.efnilite.ztd.tower

import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.tower.projectile.FallingBlockProjectile
import dev.efnilite.ztd.tower.util.TroopList
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.metadata.FixedMetadataValue

class Rock(center: Location, blocks: Set<Block>, owner: dev.efnilite.ztd.TowerPlayer, config: dev.efnilite.ztd.Config) : Tower(center, blocks, owner,
    config
) {
    private var slow: Int = 0
    private var knockback: Int = 0
    private var blockCount: Int = 1
    private var fallCount: Int = 1
    private var fallRadius: Double = 0.0
    private var data: BlockData = Material.STONE.createBlockData()

    override fun construct() {
        super.construct()

        update()
    }

    override fun upgrade() {
        update()
    }

    private fun update() {
        slow = getSpecial("slow") as Int
        knockback = getSpecial("knockback") as Int
        blockCount = getSpecial("block_count") as Int
        fallCount = getSpecial("fall_count") as Int
        fallRadius = getSpecial("fall_radius") as Double
        data = Bukkit.createBlockData(getSpecial("material").toString())
    }

    override fun shoot(troopsInRange: TroopList) {
        for (idx in 0 until blockCount) {
            val target = troopsInRange.get(idx)

            val fallingBlock = target.world.spawnFallingBlock(target.getCenter().add(0.0, 4.0, 0.0), data)
            fallingBlock.setMetadata("ztd", FixedMetadataValue(
                ZTD, FallingBlockProjectile(
                    target, this, 1.0, getDamage(),
                    fallCount = fallCount,
                    fallRadius = fallRadius,
                    knockback = knockback,
                    slow = slow
                )
            ))
//            target.targetedCount++
        }

        // todo path 3 doesnt do damage on drop
    }

    override fun getName(): String = "rock"
}