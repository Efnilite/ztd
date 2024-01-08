package dev.efnilite.ztd

import dev.efnilite.vilib.fastboard.FastBoard
import dev.efnilite.vilib.util.Strings
import dev.efnilite.ztd.session.Session
import dev.efnilite.ztd.session.Team
import dev.efnilite.ztd.world.Divider
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.time.Duration
import java.time.Instant

class TowerPlayer(val player: Player) {

    val name = player.name
    val uuid = player.uniqueId
    val board = FastBoard(player)

    val location
        get() = player.location

    val session
        get() = Divider.sessions
            .first { session -> session.getPlayer(player.uniqueId) != null }

    var coins = 0
    var team = Team.SINGLE

    fun save() {
        TODO()
    }

    fun updateBoard() {
        board.updateTitle(Strings.colour("<gradient:#6050E8:#C250E8><bold>ZTD"))

        board.updateLines(listOf("",
            "<#BC3BE7><bold>Round</bold> <gray>${session.round}/50",
            "<#BC3BE7><bold>Time</bold> <gray>${timeFromMillis(Duration.between(session.start, Instant.now()).toMillis().toInt())}",
            "",
            "<#BC3BE7><bold>Health</bold> <gray>${session.getHealth(team)}",
            "<#BC3BE7><bold>Coins</bold> <gray>$coins",
            "",
            "<dark_gray>server.ip")
            .map { line -> Strings.colour(line) })
    }

    /**
     * @param millis The duration in millis.
     * @return The formatted time.
     */
    private fun timeFromMillis(millis: Int): String {
        var millis = millis

        val h = millis / (3600 * 1000)
        millis -= h * 3600 * 1000
        val m = millis / (60 * 1000)
        millis -= m * 60 * 1000
        val s = millis / 1000
        return (if (h > 0) "$h:" else "" +
                "${padLeft(m.toString(), if (m < 10) 1 else 0)}:" +
                padLeft(s.toString(), if (s < 10) 1 else 0))
    }

    private fun padLeft(s: String, extraZeroes: Int): String {
        return if (extraZeroes > 0) String.format("%" + (extraZeroes + 1) + "s", s).replace(" ", "0") else s
    }

    fun reset() {
        with(player) {
            inventory.clear()
            closeInventory()
            resetTitle()

            saturation = 20F
            foodLevel = 20
            health = 20.0
            level = 0
            exp = 0f
            freezeTicks = 0
            fireTicks = 0
            gameMode = GameMode.SURVIVAL
            velocity = Vector(0, 0, 0)
            fallDistance = 0F

            for (potion in PotionEffectType.values()) {
                removePotionEffect(potion)
            }
        }
    }

    fun teleport(location: Location) {
        player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

    fun send(message: String) {
        player.sendMessage(Strings.colour(message))
    }

    companion object {
        private fun Player.getSession(): Session? = Divider.sessions
            .firstOrNull { session -> session.getPlayer(uniqueId) != null }

        fun Player.asTowerPlayer(): TowerPlayer? {
            val session = getSession() ?: return null
            return session.getPlayer(uniqueId)
        }

        fun Player.isTowerPlayer() = asTowerPlayer() != null
    }

}