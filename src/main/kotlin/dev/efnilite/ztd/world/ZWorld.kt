package dev.efnilite.ztd.world

import dev.efnilite.ztd.ZTD
import net.kyori.adventure.util.TriState
import org.bukkit.*
import org.codehaus.plexus.util.FileUtils
import java.io.File
import java.io.IOException

/**
 * Class for handling Parkour world generation/deletion, etc.
 */
object ZWorld {

    lateinit var world: World

    /**
     * Creates the world.
     */
    fun create() {
        world = WorldCreator("ztd")
            .generator("minecraft:air")
            .generateStructures(false)
            .biomeProvider("minecraft:plains")
            .type(WorldType.FLAT)
            .keepSpawnLoaded(TriState.FALSE)
            .createWorld()!!

        setup()
    }

    /**
     * Sets all world settings.
     */
    private fun setup() {
        world.setGameRule(GameRule.DO_FIRE_TICK, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_TILE_DROPS, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false)
        world.setGameRule(GameRule.KEEP_INVENTORY, true)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)

        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = 10000000.0
        world.difficulty = Difficulty.PEACEFUL
        world.clearWeatherDuration = 1000000
        world.isAutoSave = false
    }

    /**
     * Deletes the parkour world.
     */
    fun delete() {
        val file = File("ztd")

        if (!file.exists()) {
            return
        }

        // can't be run asynchronously
        Bukkit.unloadWorld(file.name, false)

        try {
            FileUtils.deleteDirectory(file)
        } catch (ex: IOException) {
            ZTD.instance.logging.stack("Error while trying to reset ztd world", ex)
        }
    }
}