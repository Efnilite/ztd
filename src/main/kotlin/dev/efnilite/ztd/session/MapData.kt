package dev.efnilite.ztd.session

import dev.efnilite.vilib.schematic.Schematic
import org.bukkit.util.Vector
import java.util.*

class MapData {

    var name: String = ""
    var path: MutableList<Vector> = ArrayList()
    var spawns: MutableMap<Team, Vector> = EnumMap(Team::class.java)
    lateinit var schematic: Schematic

}
