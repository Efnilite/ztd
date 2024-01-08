package dev.efnilite.ztd.tower

import dev.efnilite.vilib.particle.ParticleData
import dev.efnilite.vilib.particle.Particles
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Cuboid
import dev.efnilite.ztd.Config
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.tower.util.TroopList
import dev.efnilite.ztd.troop.Troop2
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.util.BoundingBox
import kotlin.math.roundToInt

abstract class Tower(private val center: Location, val blocks: Set<Block>, val owner: TowerPlayer, val config: Config) {

    private var shootingDamage: Int = getShootingDamage(0, 0)
    private var shootingRange: Double = getShootingRange(0, 0)
    private var shootingRate: Int = getShootingRate(0, 0)
    var targetMode = TargetMode.FIRST
    var isSold = false
    var cooldown = 0
    var path = 0
    var level = 0
    var damageInflicted = 0
        private set

    fun damage(damage: Int) {
        damageInflicted += damage
        owner.coins += damage
    }

    abstract fun upgrade()

    fun upgradeTower(path: Int, level: Int) {
        this.path = path
        this.level = level

        shootingDamage = getShootingDamage(path, level)
        shootingRange = getShootingRange(path, level)
        shootingRate = getShootingRate(path, level)

//        construct()
        upgrade()
    }

    abstract fun shoot(troopsInRange: TroopList)

    abstract fun getName(): String

    fun getSpecial(finalPath: String): Any? = getPath(finalPath, path, level)

    /**
     * Returns the top block of the current schematic. Result should be cached.
     */
    fun getTop(): Location = getCenter().set(center.x, blocks
        .filter { block -> block.isSolid }
        .maxOf { block -> block.y }.toDouble(), center.z)

    /**
     * Returns the cost of a specific path and level.
     */
    fun getCost(path: Int, level: Int): Int = getPath("cost", path, level)

    /**
     * Returns the shooting damage of a specific path and level.
     */
    fun getShootingDamage(path: Int, level: Int): Int = getPath("damage", path, level)

    /**
     * Returns the shooting range of a specific path and level.
     */
    fun getShootingRange(path: Int, level: Int): Double = getPath("range", path, level)

    /**
     * Returns the shooting rate of a specific path and level.
     */
    fun getShootingRate(path: Int, level: Int): Int = getPath("rate", path, level)

    fun getSpecialValues(path: Int, level: Int): Map<String, Any> {
        return (if (path == 0) {
            config.getChildren("paths.0")
        } else {
            config.getChildren("paths.$path.$level")
        })
            .filter { p -> p != "cost" && p != "damage" && p != "range" && p != "rate" }
            .associateWith { v -> if (path == 0) config.get("paths.0.$v") else config.get("paths.$path.$level.$v") }
    }

    private fun <T> getPath(subpath: String, path: Int, level: Int): T =
        if (path == 0) config.get("paths.0.$subpath") as T else config.get("paths.$path.$level.$subpath") as T

    fun getDamage() = shootingDamage
    fun getShootingRange() = shootingRange
    fun getShootingRate() = shootingRate
    fun getCenter(): Location = center.clone()

    fun sell() {
        destroyBlocks()
        isSold = true
        owner.coins += getSellAmount()
    }

    open fun construct() {
        destroyBlocks()

        val schematic = Schematics.getSchematic(ZTD, "${getName()}-$level.schematic")
        schematic.paste(center.clone().subtract(1.0, 0.0, 1.0))
    }

    fun destroyBlocks() {
        blocks.forEach { block -> block.type = Material.AIR }
    }

    fun getSellAmount(): Int {
        var cost = getCost(0, 0)

        if (level > 0) {
            cost += (1..level).map { lvl -> getCost(path, lvl) }
                .reduce { one: Int, two: Int -> one + two }
        }

        return (SELL_FRACTION * cost).toInt()
    }

    fun showRange() {
        // todo support double

        val circumference = 2 * Math.PI * getShootingRange()
        val particleCount = (circumference / 0.4).toInt()

        var i = 0.0
        while (i <= 5.0) {
            Particles.circle(center.clone().add(0.0, i, 0.0), ParticleData(Particle.END_ROD, null, 2), getShootingRange().roundToInt(), particleCount)
            i += 0.2
        }
    }

    companion object {

        private const val SELL_FRACTION = 0.7

        fun place(player: TowerPlayer, block: Block, type: (Location, Set<Block>, TowerPlayer) -> Tower) {
            val location = block.location.toCenterLocation()

            val foundationMin = location.clone().subtract(1.0, 1.0, 1.0)
            val foundationMax = location.clone().add(1.0, -1.0, 1.0)

            val blocksMin = location.clone().subtract(1.0, 0.0, 1.0)
            val blocksMax = location.clone().add(1.0, 10.0, 1.0)

            val foundationContainsAir = Cuboid.get(foundationMin, foundationMax, false)
                .any { b -> !b.isSolid }

            if (foundationContainsAir) {
                player.send("<red><bold>You can't place a tower here.")
                return
            }
            val blocks = Cuboid.get(blocksMin, blocksMax, false)

            val containsBlocks = blocks.any { b -> b.isSolid }

            if (containsBlocks) {
                player.send("<red><bold>You can't place a tower here.")
                return
            }

            val tower = type.invoke(location, blocks.toSet(), player)

            if (player.coins < tower.getCost(0, 0)) {
                player.send("<red><bold>You don't have enough coins.")
                return
            }

            player.coins -= tower.getCost(0, 0)
            tower.construct()
            tower.showRange()

            player.session.addTower(tower)
            player.player.closeInventory()

            if (BoundingBox.of(blocksMin, blocksMax).contains(player.location.toVector())) {
                player.teleport(tower.getTop().add(0.0, 1.0, 0.0))
            }
        }
    }
}

enum class TargetMode(val sort: (List<Troop2>) -> List<Troop2>) {

    FIRST({ list -> list }),
    LAST({ list -> list.reversed() }),
    STRONG({ list -> list.sortedBy { troop -> troop.health }}),
    WEAK({ list -> list.sortedBy { troop -> troop.health }.reversed() });

}