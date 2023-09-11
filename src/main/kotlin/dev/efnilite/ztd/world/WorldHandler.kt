package dev.efnilite.ztd.world

import dev.efnilite.vilib.util.VoidGenerator
import dev.efnilite.ztd.ZTD
import org.bukkit.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Class for handling Parkour world generation/deletion, etc.
 */
class WorldHandler {

    lateinit var world: World

    init {
        delete()
        create()
        setupIslands()
    }

    private fun create() {
        try {
            ZTD.logging.info("Creating islands game world, name = $WORLD_NAME")

            val creator: WorldCreator = WorldCreator(WORLD_NAME)
                .generateStructures(false)
                .type(WorldType.NORMAL)
                .generator(VoidGenerator.getGenerator()) // to fix No keys in MapLayer etc.
                .environment(World.Environment.NORMAL)
            val world: World? = Bukkit.createWorld(creator)

            if (world == null) {
                ZTD.logging.stack(
                    "Error while trying to create the islands world",
                    "delete the islands world folder and restart the server"
                )
            } else {
                this.world = world
            }
        } catch (ex: Throwable) {
            ZTD.logging.stack(
                "Error while trying to create/load the worlds",
                "delete the islands/lobby world folder and restart the server", ex
            )
        }
    }

    private fun setupIslands() {
        ZTD.logging.info("Setting up islands game world, name = $WORLD_NAME")
        val world: World = Bukkit.getWorld(WORLD_NAME)!!

        world.time = 1000
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_FIRE_TICK, true)
        world.setGameRule(GameRule.KEEP_INVENTORY, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.DISABLE_RAIDS, false)
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 2000000)
        world.isAutoSave = false
        world.difficulty = Difficulty.NORMAL

        ZTD.world = world
    }

    /**
     * Deletes the parkour world
     */
    fun delete() {
        ZTD.logging.info("Removing folder of island game world, name = $WORLD_NAME")

        val file = File(WORLD_NAME)

        // world has already been deleted
        if (!file.exists()) {
            return
        }
        if (!Bukkit.unloadWorld(WORLD_NAME, false)) {
            ZTD.logging.error("Failed to unload the islands world")
        }
        try {
            Files.walk(file.toPath()).use { files ->
                files.sorted(Comparator.reverseOrder())
                    .map { obj: Path -> obj.toFile() }
                    .forEach { obj: File -> obj.delete() }
            }
        } catch (ex: Exception) {
            ZTD.logging.stack("Error while trying to delete the islands world", ex)
        }
    }

    companion object {
        private const val WORLD_NAME = "ztd"
    }
}