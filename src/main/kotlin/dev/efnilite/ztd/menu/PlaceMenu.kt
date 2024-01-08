package dev.efnilite.ztd.menu

import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.animation.RandomAnimation
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.ztd.Config
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.tower.*
import org.bukkit.Material
import org.bukkit.block.Block

object PlaceMenu {

    fun open(player: TowerPlayer, block: Block) {
        val menu: Menu = Menu(3, "<white>Place")
            .item(18, Item(Material.ARROW, "<#F1A0A0>Close")
                .click({ event -> event.player.closeInventory() }))
            .item(9, Item(Material.ARROW, "<gradient:#EEC7F4:#FFFFFF><bold>Archer")
                .click({ Tower.place(player, block) { center, blocks, session ->
                    Archer(center, blocks, session, Config.ARCHER)
                } }))
            .item(10, Item(Material.FIRE_CHARGE, "<gradient:#D8342A:#E88E3A><bold>Mage")
                .click({ Tower.place(player, block) { center, blocks, session ->
                    Mage(center, blocks, session, Config.MAGE)
                } }))
            .item(11, Item(Material.ICE, "<gradient:#64FFEE:#C6F1EA><bold>Ice")
                .click({ Tower.place(player, block) { center, blocks, session ->
                    Ice(center, blocks, session, Config.ICE)
                } }))
            .item(12, Item(Material.CAULDRON, "<gradient:#148D2E:#50975F><bold>Rock")
                .click({ Tower.place(player, block) { center, blocks, session ->
                    Rock(center, blocks, session, Config.ROCK)
                } }))

        menu
            .distributeRowsEvenly()
            .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .animation(RandomAnimation())
            .open(player.player)
    }

}