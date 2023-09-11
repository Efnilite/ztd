package dev.efnilite.ztd.tower

import dev.efnilite.vilib.particle.ParticleData
import dev.efnilite.vilib.particle.Particles
import dev.efnilite.ztd.Config
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.tower.util.TroopList
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.block.Block
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Ice(center: Location, blocks: Set<Block>, owner: TowerPlayer, config: Config) : Tower(center, blocks, owner,
    config
) {

    private var freezeTime: Int = 1

    override fun construct() {
        super.construct()

        freezeTime = getSpecial("freeze_time") as Int
    }

    override fun upgrade() {
        freezeTime = getSpecial("freeze_time") as Int
    }

    override fun shoot(troopsInRange: TroopList) {
        Particles.draw(getTop(), towerParticles)

        val damage = getDamage()
        for (troop in troopsInRange) {
            troop.entity.addPotionEffect(effect.withDuration(freezeTime))

            troop.damage(this, damage)

            Particles.circle(troop.getCenter(), entityParticles, 1, 8)
        }
    }

    override fun getName(): String = "ice"

    companion object {
        val effect = PotionEffect(PotionEffectType.SLOW, 1 * 20, 0, false, false)
        val towerParticles = ParticleData(Particle.SNOW_SHOVEL, null, 10, 0.0, 0.5, 0.5, 0.5)
        val entityParticles = ParticleData(Particle.REDSTONE, DustOptions(Color.WHITE, 1f), 1)
    }
}