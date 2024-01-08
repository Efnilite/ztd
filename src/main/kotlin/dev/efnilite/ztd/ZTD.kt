package dev.efnilite.ztd

import dev.efnilite.vilib.ViPlugin
import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Logging
import dev.efnilite.vilib.util.elevator.GitElevator
import dev.efnilite.ztd.command.MapCommand
import dev.efnilite.ztd.command.ZTDCommand
import dev.efnilite.ztd.event.Events
import dev.efnilite.ztd.world.WorldHandler
import java.io.File
import java.nio.file.Files
import kotlin.io.path.name

object ZTD : ViPlugin() {

    val logging = Logging(this)
    private val handler = WorldHandler()
    val world = handler.world

    override fun enable() {
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
            paths.filter { path -> path.name.endsWith(".schematic") }
                .forEach { path ->
                    Schematics.addFromFiles(this, path.toFile()) }
        }
    }

    override fun disable() {
        handler.world.players.forEach { player -> player.kick() }
        handler.delete()
    }

    override fun getElevator(): GitElevator? = null

    fun getInFolder(file: String) = File(dataFolder, file)
}