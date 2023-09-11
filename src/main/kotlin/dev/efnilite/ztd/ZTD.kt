package dev.efnilite.ztd

import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.elevator.GitElevator
import dev.efnilite.ztd.command.MapCommand
import dev.efnilite.ztd.command.ZTDCommand
import dev.efnilite.ztd.event.Events
import dev.efnilite.ztd.world.WorldHandler
import org.bukkit.World
import java.io.File
import java.nio.file.Files
import kotlin.io.path.name

class ZTD : ViPlugin() {

    override fun enable() {
        instance = this
        logging = Logging(this)
        handler = WorldHandler()
        world = handler.world

        saveResource("maps/training.map", false)
        saveResource("maps/training.schematic", false)

        saveResource("schematics/archer-0.schematic", false)
        saveResource("schematics/ice-0.schematic", false)
        saveResource("schematics/mage-0.schematic", false)
        saveResource("schematics/rock-0.schematic", false)

        registerCommand("ztd", ZTDCommand)
        registerCommand("ztd-map", MapCommand)
        registerListener(Events)

        getInFolder("maps").mkdirs()

        registerSchematics("schematics")
        registerSchematics("maps")
    }

    private fun registerSchematics(folder: String) {
        Files.list(getInFolder(folder).toPath()).use { paths ->
            paths.filter { path -> path.name.split("\\.".toRegex())[1].lowercase() == "schematic" }
                .forEach { path ->
                    Schematics.addFromFiles(this, path.toFile()) }
        }
    }

    override fun disable() {
        handler.world.players.forEach { player -> player.kick() }
        handler.delete()
    }

    override fun getElevator(): GitElevator? = null

    companion object {
        lateinit var world: World
        lateinit var instance: ZTD
            private set
        lateinit var logging: Logging
            private set
        lateinit var handler: WorldHandler
            private set

        fun getInFolder(file: String) = File(instance.dataFolder, file)
    }
}