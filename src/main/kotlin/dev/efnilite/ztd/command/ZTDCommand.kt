package dev.efnilite.ztd.command

import dev.efnilite.vilib.command.ViCommand
import dev.efnilite.ztd.asTowerPlayer
import dev.efnilite.ztd.menu.PlayMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ZTDCommand : ViCommand() {

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        val player = sender as Player
        when (args[0]) {
            "play" -> PlayMenu.open(player)
            "leave" -> {
                val tp = player.asTowerPlayer() ?: return true

                tp.session.leave(tp)
            }
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}