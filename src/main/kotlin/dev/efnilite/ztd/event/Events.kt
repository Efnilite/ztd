package dev.efnilite.ztd.event

import dev.efnilite.vilib.event.EventWatcher
import dev.efnilite.ztd.TowerPlayer.Companion.asTowerPlayer
import dev.efnilite.ztd.TowerPlayer.Companion.isTowerPlayer
import dev.efnilite.ztd.menu.PlaceMenu
import dev.efnilite.ztd.menu.TowerMenu
import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.tower.projectile.BounceProjectile
import dev.efnilite.ztd.tower.projectile.FallingBlockProjectile
import dev.efnilite.ztd.tower.projectile.Projectile
import dev.efnilite.ztd.tower.util.TowerUtil
import dev.efnilite.ztd.tower.util.TroopList
import dev.efnilite.ztd.world.ZWorld
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


object Events : EventWatcher {

    @EventHandler
    fun spawn(event: CreatureSpawnEvent) {
        if (event.entity.world.uid != ZWorld.world.uid) {
            return
        }

        if (event.entityType == EntityType.CHICKEN) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun rightClick(event: PlayerInteractEvent) {
        val player = event.player.asTowerPlayer()
        val block = event.clickedBlock

        if (player == null || block == null || event.action != Action.RIGHT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND) {
            return
        }

        val tower: Tower? = player.session.getTowers().firstOrNull { tower -> tower.blocks.contains(block) }

        if (tower == null) {
            PlaceMenu.open(player, block.getRelative(BlockFace.UP))
        } else {
            TowerMenu.open(player, tower)
        }
    }

    @EventHandler
    fun spawn(event: EntityDropItemEvent) {
        handleRockDrop(event.entity, event)
    }

    @EventHandler
    fun change(event: EntityChangeBlockEvent) {
        handleRockDrop(event.entity, event)
    }

    private fun handleRockDrop(entity: Entity, event: Cancellable) {
        if (entity.world.uid != ZWorld.world.uid || entity.type != EntityType.FALLING_BLOCK) {
            return
        }

        val projectile: FallingBlockProjectile = entity.getMetadata("ztd")[0].value() as FallingBlockProjectile
        val slow = PotionEffect(PotionEffectType.SLOW, 20, projectile.slow, false, false)
        val damage = projectile.getProjectileDamage()
        projectile.target.damage(projectile.owner, damage)

        if (projectile.fallRadius > 0 && projectile.fallCount > 0) {
            val troopList = TroopList(projectile.target.session.getNearbyTroops(
                projectile.target.getCenter(),
                projectile.fallRadius
            )
                .filter { troop -> troop.uuid != projectile.target.uuid }
                .take(projectile.fallCount))

            println(troopList.troops.size)

            for (troop in troopList) {
                if (projectile.knockback > 0) {
                    val reversed = troop.path.getHeading(troop, troop.getSpeed()).multiply(-1)

                    troop.entity.teleport(troop.location.add(reversed))
                }
                if (projectile.slow > 0) {
                    troop.entity.addPotionEffect(slow)
                }

                troop.damage(projectile.owner, damage)
            }
        }

        entity.remove()
        event.isCancelled = true
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        if (event.player.world.uid != ZWorld.world.uid) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun drop(event: BlockDropItemEvent) {
        if (event.block.world.uid != ZWorld.world.uid) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun damage(event: EntityDamageEvent) {
        if (event.entity.world.uid != ZWorld.world.uid) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun projectileHit(event: ProjectileHitEvent) {
        val entity = event.entity

        if (entity.world.uid != ZWorld.world.uid) {
            return
        }

        val projectile: Projectile = entity.getMetadata("ztd")[0].value() as Projectile
        val damage = projectile.getProjectileDamage()
        projectile.target.damage(projectile.owner, damage)

        if (projectile is BounceProjectile) {
            val list = TroopList(projectile.target.session.getNearbyTroops(
                projectile.target.getCenter(),
                projectile.bounceRadius
            )
                .filter { troop -> troop.uuid != projectile.target.uuid })

            if (!list.isEmpty() && projectile.bounceCount > 0) {
                val target = list.get(0)

                TowerUtil.shootArrow(
                    target.getCenter().add(0.0, target.entity.boundingBox.height, 0.0),
                    target,
                    projectile.getNewInstance(target),
                    projectile.speed
                )
            }
        }

        entity.remove()
//        projectile.target.targetedCount -= 1
    }

    @EventHandler
    fun leave(event: PlayerQuitEvent) {
        val player = event.player.asTowerPlayer() ?: return

        player.session.leave(player)
    }

    @EventHandler
    fun destroyBlock(event: BlockBreakEvent) {
        if (event.player.isTowerPlayer()) {
            event.isCancelled = true
        }
    }
}