package dev.efnilite.ztd.menu

import dev.efnilite.vilib.inventory.PagedMenu
import dev.efnilite.vilib.inventory.animation.WaveEastAnimation
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.vilib.inventory.item.MenuItem
import dev.efnilite.ztd.session.Session
import org.bukkit.Material
import org.bukkit.entity.Player

object PlayMenu {

    fun open(player: Player) {
        val menu = PagedMenu(2, "<white>Play")

        val items: MutableList<MenuItem> = ArrayList()
        for (map in Session.getAvailableMaps()) {
            items.add(Item(Material.LIME_STAINED_GLASS_PANE, "<#B750E8><bold>${map}")
                .lore("<dark_gray>Click to play this map!")
                .click({ event ->
                    Session.create(player, map)
                    event.player.closeInventory()
                }))
        }

        menu.addToDisplay(items).displayRows(0)
            .prevPage(9, Item(Material.RED_DYE, "<red><bold>Previous page").click({ menu.page(-1) }))
            .nextPage(17, Item(Material.LIME_DYE, "<green><bold>Next page").click({ menu.page(1) }))
            .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .animation(WaveEastAnimation())
            .open(player)
    }
}