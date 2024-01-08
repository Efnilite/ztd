package dev.efnilite.ztd.command

import dev.efnilite.vilib.command.ViCommand
import dev.efnilite.vilib.schematic.Schematic
import dev.efnilite.vilib.util.Cuboid
import dev.efnilite.vilib.util.Locations
import dev.efnilite.ztd.ZTD
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

private data class MapContainer(
    val name: String,
    val path: List<Vector>,
    val spawns: Map<Team, Vector>,
    val min: Location
)

object MapCommand : ViCommand() {

    private val ids: MutableMap<String, MapContainer> = HashMap()

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (!sender.isOp || sender !is Player) {
            return false
        }

        when (args.size) {
            2 -> args2(sender, args)
            3 -> args3(sender, args)
            4 -> args4(sender, args)
            else -> sendHelp(sender)
        }
        return true
    }

    private fun args2(player: Player, args: Array<String>) {
        val map = args[1].lowercase()

        when (args[0].lowercase()) {
            "create" -> {
                ids[map] = MapContainer("", emptyList(), emptyMap(), player.location)

                player.sendMessage("Created map $map")
            }

            "export" -> {
                val container = ids[args[1].lowercase()]!!

                try {
                    BukkitObjectOutputStream(BufferedOutputStream(FileOutputStream(ZTD.getInFolder("maps/$map.map")))).use { writer ->
                        writer.writeObject(container.path)
                        writer.writeObject(container.spawns)
                        writer.flush()
                        player.sendMessage("Exported file successfully.")
                    }
                } catch (ex: Exception) {
                    player.sendMessage("Error while trying to export file, check your arguments.")
                    ex.printStackTrace()
                }
            }

            "add-path" -> {
                val container = ids[map]!!
                val existing = container.path.toMutableList()

                existing.add(player.location.toBlockLocation().subtract(container.min!!).toVector())

                ids[map] = MapContainer(container.name, existing, container.spawns, container.min)

                player.sendMessage(
                    "Added path at ${player.location.toBlockLocation().subtract(container.min).toVector()}"
                )
            }

            else -> sendHelp(player)
        }
    }

    private fun args3(player: Player, args: Array<String>) {
        val map = args[1].lowercase()

        when (args[0].lowercase()) {
            "add-spawn" -> {
                val container = ids[map]!!
                val team = Team.valueOf(args[2].uppercase())
                val spawns = container.spawns.toMutableMap()

                spawns[team] = player.location.toBlockLocation().subtract(container.min).toVector()

                ids[map] = MapContainer(container.name, container.path, spawns, container.min)

                player.sendMessage("Added spawn for team ${team.name}")
            }

            "tower" -> {
                val vector1 = parse(args[1])
                val vector2 = parse(args[2])
                val world = player.world

                Schematic.create().save(
                    "${ZTD.dataFolder}/schematics/${UUID.randomUUID()}.schematic",
                    vector1.toLocation(world),
                    vector2.toLocation(world),
                    ZTD
                )
            }
        }
    }

    private fun args4(player: Player, args: Array<String>) {
        val map = args[1].lowercase()

        if (args[0].lowercase() == "corners") {
            val container = ids[map]!!
            val vector1 = parse(args[2])
            val vector2 = parse(args[3])
            val world: World = player.world

            Schematic.create().save(
                "${ZTD.dataFolder}/maps/$map.schematic",
                vector1.toLocation(world),
                vector2.toLocation(world),
                ZTD
            )

            player.sendMessage("Saving schematic from $vector1 to $vector2")

            Cuboid.getAsync(vector1.toLocation(world), vector2.toLocation(world), true, ZTD) { blocks ->
                ids[map] = MapContainer(container.name, container.path, container.spawns, blocks
                    .map(Block::getLocation)
                    .reduce { pos1: Location, pos2: Location -> Locations.min(pos1, pos2) })

                player.sendMessage("Set box from $vector1 to $vector2")
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }

    private fun parse(string: String): Vector {
        val values = string.replace("[()]".toRegex(), "")
            .replace(",", " ")
            .split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

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
