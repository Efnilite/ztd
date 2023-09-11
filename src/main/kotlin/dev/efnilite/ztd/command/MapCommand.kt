package dev.efnilite.ztd.command

import dev.efnilite.vilib.command.ViCommand
import dev.efnilite.vilib.schematic.Schematic
import dev.efnilite.vilib.util.Cuboid
import dev.efnilite.vilib.util.Locations
import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.session.MapData
import dev.efnilite.ztd.session.Team
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.*

object MapCommand : ViCommand() {

    private val ids: MutableMap<String, MapContainer> = HashMap()

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (!sender.isOp || sender !is Player) {
            return false
        }
        when (args.size) {
            2 -> {
                when (args[0].lowercase()) {
                    "create" -> {
                        ids[args[1].lowercase()] =
                            MapContainer(
                                MapData(),
                                null
                            )

                        sender.sendMessage("Created map ${args[1].lowercase()}")
                    }

                    "export" -> {
                        val container = ids[args[1].lowercase()]!!
                        try {
                            BukkitObjectOutputStream(BufferedOutputStream(FileOutputStream(ZTD.getInFolder("maps/${args[1]}.map")))).use { writer ->
                                writer.writeObject(container.data.path)
                                writer.writeObject(container.data.spawns)
                                writer.flush()
                                sender.sendMessage("Exported file successfully.")
                            }
                        } catch (ex: Exception) {
                            sender.sendMessage("Error while trying to export file, check your arguments.")
                            ex.printStackTrace()
                        }
                    }

                    "add-path" -> {
                        val container = ids[args[1].lowercase()]
                        val map: MapData = container!!.data
                        val existing: MutableList<Vector> = map.path

                        existing.add(sender.location.toBlockLocation().subtract(container.min!!).toVector())

                        map.path = existing

                        sender.sendMessage(
                            "Added path at ${
                                sender.location.toBlockLocation().subtract(container.min!!).toVector()
                            }"
                        )
                    }

                    else -> {
                        sendHelp(sender)
                    }
                }
            }

            3 -> {
                when (args[0].lowercase()) {
                    "add-spawn" -> {
                        val container = ids[args[1].lowercase()]
                        val map = container!!.data
                        val team = Team.valueOf(args[2])

                        map.spawns[team] = sender.location.toBlockLocation().subtract(container.min!!).toVector()
                        sender.sendMessage("Added spawn for team ${team.name}")
                    }
                    "tower" -> {
                        val vector1 = parse(args[1])
                        val vector2 = parse(args[2])
                        val world: World = sender.world

                        Schematic.create().save(
                            "${ZTD.instance.dataFolder}/schematics/${UUID.randomUUID()}.schematic",
                            vector1.toLocation(world),
                            vector2.toLocation(world)
                        )
                    }
                }
            }

            4 -> {
                if (args[0].lowercase() == "corners") {
                    val container = ids[args[1].lowercase()]
                    val vector1 = parse(args[2])
                    val vector2 = parse(args[3])
                    val world: World = sender.world

                    Schematic.create().save(
                        "${ZTD.instance.dataFolder}/maps/${args[1]}.schematic",
                        vector1.toLocation(world),
                        vector2.toLocation(world)
                    )

                    sender.sendMessage("Saving schematic from $vector1 to $vector2")

                    Cuboid.getAsync(vector1.toLocation(world), vector2.toLocation(world), true) { blocks ->
                        container!!.min = blocks
                            .map(Block::getLocation)
                            .reduce { pos1: Location, pos2: Location -> Locations.min(pos1, pos2) }

                        sender.sendMessage("Set box from $vector1 to $vector2")
                    }
                }
            }

            else -> sendHelp(sender)
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }

    private data class MapContainer(val data: MapData, var min: Location?)

    private fun parse(string: String): Vector {
        val values = string.replace("[()]".toRegex(), "")
            .replace(",", " ")
            .split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Vector(values[0].toDouble(), values[1].toDouble(), values[2].toDouble())
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("================== LI Map Setup ==================")
        sender.sendMessage("/li-map create <map-name> - Create a new map instance to edit.")
        sender.sendMessage("/li-map corners <map-name> <location, ex: 10,10,10> <location, ex: -10,-10,-10> - Add two locations as the corner locations")
        sender.sendMessage("/li-map add-path <map-name> - Add your current location as path location")
        sender.sendMessage("/li-map add-spawn <map-name> <team-name> - Add your current location as spawn location")
        sender.sendMessage("/li-map export <map-name> - Creates a .json file with all saved data.")
    }
}
