package dev.efnilite.ztd.troop

import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.session.Session
import dev.efnilite.ztd.tower.Mage
import dev.efnilite.ztd.tower.Tower
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.math.max

/**
 * Represents any troop. Iteration 2.
 */
class Troop2(val type: TroopType, val path: Path, val owner: TowerPlayer, data: PersistentTroopData = PersistentTroopData(path.getTarget())) {

    var health = type.health
    val entity: LivingEntity = ZTD.world.spawnEntity(data.location, type.type) as LivingEntity

    val world: World
        get() = entity.world
    val location: Location
        get() = entity.location
    val uuid: UUID
        get() = entity.uniqueId
    val session: Session
        get() = owner.session

    init {
        entity.setAI(false)
        entity.isSilent = true
        entity.equipment?.clear()
        entity.fireTicks = data.fireTicks
        data.activePotionEffects.forEach(entity::addPotionEffect)

        type.onSpawn.invoke(entity)

        session.addTroop(this)
    }

    /**
     * Ticks this troop.
     */
    fun tick() {
        if (entity.fireTicks % 20 != 1) return
        val data = getFireData() ?: return

        damage(data.owner, data.damage)
    }

    /**
     * Damages this troop. [tower] is the owning tower. [initialDamage] is the damage before bonus damage is applied.
     */
    fun damage(tower: Tower, initialDamage: Int) {
        val damage = initialDamage + (getFireData()?.damageBonus ?: 0)

        if (damage <= 0) return

        health -= damage
        tower.damage(damage)

        if (health <= 0) {
            session.removeTroop(this)
            entity.remove()

            val newType = TroopType.getTroopFromHealth(
                TroopType.getHealthFromTroop(
                    type
                ) - damage
            ) ?: return
            val next = TroopType.Companion.getNext(newType)

            var totalSpawned = 0
            for ((amount, type) in next.onDeath.invoke()) {
                totalSpawned += amount
                repeat(amount) {
                    Troop2(
                        type,
                        path.clone(),
                        owner,
                        PersistentTroopData(location, entity.fireTicks, entity.activePotionEffects)
                    )
                }
            }

            if (totalSpawned > 1) {
                println("amount spawned $totalSpawned")
            }

            return
        }
    }

    private fun getFireData(): Mage.FireData? {
        val metadata = entity.getMetadata("ztd fire")
        if (metadata.isEmpty()) return null
        val value = metadata[metadata.size - 1]
        return value.value() as Mage.FireData
    }

    /**
     * Snaps this troop to the block under the troop to avoid floating.
     */
    fun snapToGround() {
        val location = location
        var currentBlock = location.block

        if (currentBlock.type.isAir) {
            currentBlock = currentBlock.getRelative(BlockFace.DOWN)
        }

        location.y = currentBlock.boundingBox.maxY

        entity.teleport(location)
    }

    /**
     * Returns the speed in blocks/tick.
     */
    fun getSpeed(): Double {
        val effect = entity.getPotionEffect(PotionEffectType.SLOW) ?: return type.speed
        return max(0.0, (0.4 - 0.1 * effect.amplifier) * type.speed)
    }

    /**
     * Returns the center of the entity.
     */
    fun getCenter() = entity.boundingBox.center.toLocation(world)
}