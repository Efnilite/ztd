package dev.efnilite.ztd.troop

import org.bukkit.Location
import org.bukkit.potion.PotionEffect

data class PersistentTroopData(val location: Location, val fireTicks: Int = 0, val activePotionEffects: Collection<PotionEffect> = emptySet())
