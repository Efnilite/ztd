package dev.efnilite.ztd.session

import dev.efnilite.vilib.schematic.Schematics
import dev.efnilite.vilib.util.Cuboid
import dev.efnilite.vilib.util.Strings
import dev.efnilite.vilib.util.Task
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.ZTD
import dev.efnilite.ztd.isTowerPlayer
import dev.efnilite.ztd.tower.Tower
import dev.efnilite.ztd.tower.util.TroopList
import dev.efnilite.ztd.troop.Path2
import dev.efnilite.ztd.troop.Troop2
import dev.efnilite.ztd.troop.TroopType
import dev.efnilite.ztd.world.Divider
import dev.efnilite.ztd.world.ZWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.bukkit.util.io.BukkitObjectInputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.time.Instant
import java.util.*
import kotlin.math.max

class Session(private val map: String) {

    private var tick: Int = 0

    private var waypoints: List<Vector> = emptyList()
    private var spawns: Map<Team, Location> = EnumMap(Team::class.java)

    private var sendQueueInterval: Map<TroopType, Int> = EnumMap(TroopType::class.java)
    private var sendQueueCounts: MutableMap<TroopType, Int> = EnumMap(TroopType::class.java)

    private val towers: MutableSet<Tower> = HashSet()
    private val troops: MutableMap<UUID, Troop2> = HashMap()
    private val players: MutableMap<UUID, TowerPlayer> = HashMap()
    private val health: MutableMap<Team, Int> = EnumMap(Team::class.java)
    private lateinit var allowedArea: BoundingBox

    lateinit var start: Instant
        private set

    private lateinit var task: BukkitTask

    private var inLobby = true
    var round = 0
        private set

    private fun construct(onComplete: (Session) -> Unit) {
        val schematic = Schematics.getSchematic(ZTD.instance, "$map.schematic")

        ZTD.instance.logging.info("Constructing game map, map = $map")

        // offset center by half the dimensions of the schematic to
        // paste the schematic in the center.
        val center = Divider.toLocation(this)
        val minSchematic = center.clone().subtract(schematic.dimensions.multiply(0.5))

        ZTD.instance.logging.info("Pasting game map schematic, map = $map")
        schematic.paste(minSchematic)

        val halfDimensions: Vector = schematic.dimensions.clone().multiply(0.75)
        val minMap: Location = center.clone().subtract(halfDimensions)
        val maxMap: Location = center.clone().add(halfDimensions)

        allowedArea = BoundingBox.of(minMap, maxMap)

        // todo cache
        Task.create(ZTD.instance)
            .async()
            .execute {
                // map data handling
                ZTD.instance.logging.info("Reading game map data, map = $map")

                BukkitObjectInputStream(BufferedInputStream(FileInputStream(ZTD.instance.getInFolder("maps/$map.map")))).use { stream ->
                    ZTD.instance.logging.info("Deserializing map data, map = $map")

                    waypoints = (stream.readObject() as MutableList<Vector>)
                        .map { vector ->
                            vector.clone()
                                .add(minSchematic.toVector())
                                .add(Vector(0.5, 0.0, 0.5))
                        }
                        .toList()

                    spawns = (stream.readObject() as MutableMap<Team, Vector>)
                        .map { (team, vector) ->
                            team to vector.clone().toLocation(ZWorld.world)
                                .add(minSchematic)
                                .add(0.5, 0.0, 0.5)
                        }
                        .toMap()
                }

                // go to main thread
//                Task.create(ZTD.instance).execute { onComplete.invoke(this) }.run()
                onComplete.invoke(this)
            }
            .run()
    }

    fun getHealth(team: Team) = health.getOrDefault(team, 150)

    fun addTower(tower: Tower) {
        towers.add(tower)
    }

    fun getTowers(): Set<Tower> = towers

    fun addTroop(troop: Troop2) {
        troops[troop.uuid] = troop
    }

    fun removeTroop(troop: Troop2) {
        troops.remove(troop.uuid)
    }

    fun getTroop(uuid: UUID): Troop2? = troops[uuid]

    fun getPlayer(uuid: UUID): TowerPlayer? = players[uuid]

    fun damageTeam(team: Team, damage: Int) {
        val newHealth = max(0, health.getOrDefault(team, 150) - damage)
        health[team] = newHealth

        if (newHealth == 0) {
            win()
        }
    }

    @Synchronized
    fun join(pl: Player) {
        // cant join if game has already started or player is already in a game
        if (!inLobby || pl.isTowerPlayer()) {
            return
        }

        ZTD.instance.logging.info("Handling join of player, player = ${pl.name}")

        val player = TowerPlayer(pl)
        players[player.uuid] = player

        // show player to other players in game
        for (op in Bukkit.getOnlinePlayers()) {
            if (op.isTowerPlayer()) {
                op.showPlayer(ZTD.instance, pl)
            } else {
                op.hidePlayer(ZTD.instance, pl)
            }
        }

        // show other players to player in game
        for (op in Bukkit.getOnlinePlayers()) {
            if (op.isTowerPlayer()) {
                pl.showPlayer(ZTD.instance, op)
            } else {
                pl.hidePlayer(ZTD.instance, op)
            }
        }

        for (loopPlayer in players.values) {
            // todo fix amount
            loopPlayer.send("<gray>${player.name} joined <dark_gray>(0 spaces left)")
        }

        player.teleport(spawns[Team.SINGLE]!!)

        player.reset()

        pl.allowFlight = true
        pl.isFlying = true

        start()
    }

    fun leave(player: TowerPlayer) {
        ZTD.instance.logging.info("Handling leave of player, player = ${player.name}")

        // avoid double leave handling
        if (!players.containsValue(player)) {
            return
        }

        // show player who left to all other players who aren't in game
        for (op in Bukkit.getOnlinePlayers()) {
            if (op.isTowerPlayer()) {
                op.hidePlayer(ZTD.instance, player.player)
            } else {
                op.showPlayer(ZTD.instance, player.player)
            }
        }

        // show all other players who aren't in game to player
        for (op in Bukkit.getOnlinePlayers()) {
            if (op.isTowerPlayer() && op != player.player) {
                player.player.hidePlayer(ZTD.instance, op)
            } else {
                player.player.showPlayer(ZTD.instance, op)
            }
        }

        players.remove(player.uuid)

        player.board.delete()
        player.reset()
        player.player.kickPlayer("bye!")

        if (players.isEmpty()) {
            win()
        }
    }

    fun win() {
        task.cancel()

        destroy()

        troops.values.forEach { troop -> troop.entity.remove() }
        troops.clear()
        towers.clear()
    }

    fun destroy() {
        // get all blocks in the modifiable area that need to be set to air
        Cuboid.getAsync(allowedArea.min.toLocation(ZWorld.world), allowedArea.max.toLocation(ZWorld.world), true, ZTD.instance)

        { blocks ->
            ZTD.instance.logging.info("Gathered blocks in modifiable range, blocks = ${blocks.size}")

            val air = Material.AIR.createBlockData()

            Cuboid.set(blocks.associateWith { air }, ZTD.instance) {

                ZTD.instance.logging.info("Finished map block reset")

                Divider.remove(this)
            }
        }
    }

    fun start() {
        start = Instant.now()

        task = Task.create(ZTD.instance)
            .repeat(1)
            .execute(object : BukkitRunnable() {
                override fun run() {
                    if (players.isEmpty()) {
                        this.cancel()
                        return
                    }

                    tick()
                }

            })
            .run()
    }

    private fun queueTroops() {
        sendQueueCounts = TroopType.values()
            .associateWith { type -> type.spawnAmount.invoke(round).toInt() }
            .filter { (_, amount) -> amount > 0 }
            .toMutableMap()

        sendQueueInterval = TroopType.values().associateWith { type -> type.spawnSpeed.invoke(round).toInt() }
    }

    fun tick() {
        tickTowers()
        tickTroops()

        if (troops.isEmpty() && sendQueueCounts.isEmpty()) {
            if (round == 50) {
                players.values.forEach { player -> leave(player) }

                win()
                return
            }

            round++
            queueTroops()

            players.values.forEach { player ->
                player.coins += 300
                player.player.sendTitle(" ", Strings.colour("<gradient:#6050E8:#C250E8><bold>Round $round"), 5, 10, 5)
            }
        }

        if (sendQueueCounts.isNotEmpty()) {
            for ((type, count) in HashMap(sendQueueCounts)) {// todo replace with .forEach?

                println(type)
                println(count)

                val interval = sendQueueInterval[type]!!

                if (tick % interval != 0) continue

                Troop2(type, Path2(waypoints, type.speed), players.values.random())

                val newCount = count - 1

                if (newCount == 0) {
                    sendQueueCounts.remove(type)
                    continue
                }
                sendQueueCounts[type] = newCount
            }
        }

        for (player in players.values) {
//            if (!allowedArea.contains(player.location.toVector())) {
//                player.teleport(spawns[player.team]!!)
//            }

            player.updateBoard()
        }

        tick++
    }

    private fun tickTowers() {
        for (tower in HashSet(towers)) {
            tower.cooldown -= 1

            if (tower.cooldown > 0) continue

            if (tower.isSold) {
                towers.remove(tower)
                continue
            }

            val nearbyEntities = getNearbyTroops(tower.getCenter(), tower.getShootingRange())

            if (nearbyEntities.isEmpty()) continue

            tower.shoot(TroopList(tower.targetMode.sort.invoke(nearbyEntities)))
            tower.cooldown = tower.getShootingRate()
        }
    }

    fun getNearbyTroops(location: Location, range: Double): List<Troop2> = location
        .getNearbyLivingEntities(range) { entity -> entity.type != EntityType.PLAYER && !entity.isDead }
        .mapNotNull { entity -> getTroop(entity.uniqueId) }
//        .filter { troop -> troop.targetedCount < troop.health }
        .toList()

    private fun tickTroops() {
        for (troop in HashSet(troops.values)) {
            val entity = troop.entity

            if (entity.isDead) {
                troops.remove(entity.uniqueId)
                continue
            }

            troop.tick()

            if (troop.path.points.size <= troop.index) {
                continue
            }

            entity.teleport(troop.path.points[troop.index].toLocation(ZWorld.world))

            troop.index++

            troop.snapToGround()
        }
    }

    companion object {

        /**
         * Creates a new builder.
         *
         * @return A new builder instance.
         */
        fun create(player: Player, map: String): Session {
            player.sendMessage(Strings.colour("<gray>Creating map..."))

            val session = Session(map)

            Divider.add(session)

            session.construct { it.join(player) }

            return session
        }

        // todo cache
        fun getAvailableMaps(): List<String> {
            try {
                Files.list(ZTD.instance.getInFolder("maps").toPath())
                    .use { maps ->
                        return maps
                            .map { p ->
                                p.toFile().name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                            }
                            .distinct()
                            .toList()
                    }
            } catch (ex: IOException) {
                ZTD.instance.logging.stack("Error while trying to find available maps", ex)
                return emptyList()
            }
        }
    }
}