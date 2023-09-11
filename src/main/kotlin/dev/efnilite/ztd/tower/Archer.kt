package dev.efnilite.ztd.tower

import dev.efnilite.ztd.Config
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.tower.projectile.BounceProjectile
import dev.efnilite.ztd.tower.projectile.FinalBounceProjectile
import dev.efnilite.ztd.tower.projectile.IncrementalBounceProjectile
import dev.efnilite.ztd.tower.util.TowerUtil
import dev.efnilite.ztd.tower.util.TroopList
import org.bukkit.Location
import org.bukkit.block.Block
import kotlin.math.roundToInt

class Archer(center: Location, blocks: Set<Block>, owner: TowerPlayer, config: Config) : Tower(
    center, blocks, owner, config
) {

    private var distanceDamage: Int = 1
    private var arrowCount: Int = 1
    private var shootLocation: Location = center

    override fun construct() {
        super.construct()

        distanceDamage = getSpecial("distance_damage") as Int
        arrowCount = getSpecial("arrow_count") as Int
        shootLocation = getTop().add(0.0, 2.0, 0.0)
    }

    override fun upgrade() {
        distanceDamage = getSpecial("distance_damage") as Int
        arrowCount = getSpecial("arrow_count") as Int
        shootLocation = getTop().add(0.0, 2.0, 0.0)
    }

    override fun shoot(troopsInRange: TroopList) {
        when (path) {
            1 -> for (idx in 0 until arrowCount) {
                TowerUtil.shootArrow(shootLocation, troopsInRange.get(idx), this)
            }

            2 -> {
                when (level) {
                    2 -> {
                        TowerUtil.shootArrow(shootLocation, troopsInRange.get(0), this,
                            if (troopsInRange.get(0).location.distanceSquared(getCenter()) > 0.5 * (getShootingRange() * getShootingRange())) getDamage()
                            else getDamage() + 1)
                    }
                    4, 5 -> {
                        val t: Double = troopsInRange.get(0).location.distanceSquared(getCenter()) / (getShootingRange() * getShootingRange())

                        TowerUtil.shootArrow(shootLocation, troopsInRange.get(0), this, getDamage() + (t * distanceDamage).roundToInt())
                    }
                    else -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0), this)
                }
            }

            3 -> {
                when (level) {
                    2 -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0),
                        BounceProjectile(troopsInRange.get(0), this, 4.5, getDamage(), 1, 2.0)
                    )
                    3 -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0),
                        FinalBounceProjectile(
                            troopsInRange.get(0),
                            this,
                            4.5,
                            getDamage(),
                            3,
                            2.0
                        )
                    )
                    4 -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0),
                        FinalBounceProjectile(
                            troopsInRange.get(0),
                            this,
                            4.5,
                            getDamage(),
                            3,
                            4.0
                        )
                    )
                    5 -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0),
                        IncrementalBounceProjectile(
                            troopsInRange.get(0),
                            this,
                            4.5,
                            getDamage(),
                            9,
                            6.0
                        )
                    )
                }
            }

            else -> TowerUtil.shootArrow(shootLocation, troopsInRange.get(0), this)  // level 0
        }
    }

    override fun getName(): String = "archer"

}