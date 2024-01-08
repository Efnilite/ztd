package dev.efnilite.ztd.menu

import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.animation.RandomAnimation
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.vilib.inventory.item.SliderItem
import dev.efnilite.ztd.TowerPlayer
import dev.efnilite.ztd.tower.TargetMode
import dev.efnilite.ztd.tower.Tower
import org.bukkit.Material

private data class LevelData(val material: Material, val main: String, val accent: String)

private fun String.beautify(): String = replace("_", " ")
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

object TowerMenu {

    fun open(player: TowerPlayer, tower: Tower) {
        val menu: Menu = Menu(6, "<white>${tower.javaClass.simpleName}")
            .item(49, Item(Material.BARREL, "<#3BB8E7><bold>${tower.damageInflicted}")
                .lore("<gray>Damage inflicted by this tower."))
            .item(50, SliderItem()
                .initial(tower.targetMode.ordinal)
                .add(0, Item(Material.SPYGLASS, "<#3BE79F><bold>First")
                    .lore("<gray>Select which troops are targeted first.")) {
                    tower.targetMode = TargetMode.LAST
                    true }
                .add(1, Item(Material.SPYGLASS, "<#3BE7BB><bold>Last")
                    .lore("<gray>Select which troops are targeted first.")) {
                    tower.targetMode = TargetMode.STRONG
                    true }
                .add(2, Item(Material.SPYGLASS, "<#3BE7D7><bold>Strong")
                    .lore("<gray>Select which troops are targeted first.")) {
                    tower.targetMode = TargetMode.WEAK
                    true }
                .add(3, Item(Material.SPYGLASS, "<#3BD7E7><bold>Weak")
                    .lore("<gray>Select which troops are targeted first.")) {
                    tower.targetMode = TargetMode.FIRST
                    true }
            )
            .item(51, Item(Material.BARRIER, "<#EF3DAD><bold>Sell")
                .lore("<gray>Sell this tower for <#E7A4CE>${tower.getSellAmount()} coins<gray>.")
                .click({ event ->
                    tower.sell()
                    event.player.closeInventory()
                }))
            .item(52, Item(Material.ARROW, "<#F1A0A0><bold>Close")
                .click({ event -> event.player.closeInventory() }))

        showPaths(tower, player, menu)
            .distributeRowsEvenly()
            .fillBackground(Material.GRAY_STAINED_GLASS_PANE)
            .animation(RandomAnimation())
            .open(player.player)

        tower.showRange()
    }

    private fun showPaths(tower: Tower, player: TowerPlayer, menu: Menu): Menu {
        val currentRange = tower.getShootingRange()
        val currentDamage = tower.getDamage()
        val currentRate = tower.getShootingRate()

        for (path in 1 until tower.config.getChildren("paths").size) {
            for (level in 1..5) {
                val isPath = tower.path > 0 && tower.path == path
                val isNotUpgraded = tower.path == 0 && level == 1

                val data: LevelData = when {
                    (level == tower.level + 1 && isPath) || isNotUpgraded -> LevelData(
                        Material.ORANGE_STAINED_GLASS_PANE,
                        "<#FF8728>",
                        "<#FFAF6F>"
                    )
                    tower.level >= level && isPath -> LevelData(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "<#52FF28>",
                        "<#A3FF8C>"
                    )
                    else -> LevelData(Material.RED_STAINED_GLASS_PANE, "<#FF6767>", "<#FF9A9A>")
                }

                val item = Item(data.material, "${data.main}<bold>Level $level")
                val lore: MutableList<String> = mutableListOf()

                val cost = tower.getCost(path, level)
                val range = tower.getShootingRange(path, level)
                val damage = tower.getShootingDamage(path, level)
                val rate = tower.getShootingRate(path, level)

                lore.addAll(listOf(
//                    "<gray>Desc.",
                    "",
                    "${data.main}<bold>Stats",
                    "<dark_gray>• ${data.accent}Cost <gray>$cost",
                    "<dark_gray>• ${data.accent}Range <gray>$range ${getStatChange(currentRange, range)}",
                    "<dark_gray>• ${data.accent}Damage <gray>$damage ${getStatChange(currentDamage, damage)}",
                    "<dark_gray>• ${data.accent}Rate <gray>$rate ${getStatChange(currentRate, rate, -1)}"))

                val values = tower.getSpecialValues(path, level)
                if (values.isNotEmpty()) {
                    lore.add("")
                    lore.add("${data.main}<bold>Special Stats")
                    for ((name, value) in values) {
                        val current = tower.getSpecial(name)

                        val statChange = try {
                            getStatChange(
                                current.toString().toDouble(),
                                value.toString().toDouble()
                            )
                        } catch (ex: NumberFormatException) {
                            ""
                        }

                        lore.add("<dark_gray>• ${data.accent}${name.beautify()} <gray>$value $statChange")
                    }
                }

                if ((isPath && level == tower.level + 1) || isNotUpgraded) {
                    item.click({
                        if (player.coins >= tower.getCost(path, level)) {
                            tower.upgradeTower(path, level)
                            player.coins -= tower.getCost(path, level)
                            open(player, tower)
                        } else {
                            player.send("<red><bold>You don't have enough coins.")
                        }
                    })
                }

                menu.item(path * 9 + level, item.lore(lore))
            }
        }

        return menu
    }

    private fun getStatChange(current: Number, new: Number, positive: Int = 1): String {
        return when (new.toDouble().compareTo(current.toDouble())) {
            positive -> "<#48D625>($current ⇒ $new)"
            -positive -> "<#F22C2C>($current ⇒ $new)"
            else -> ""
        }
    }
}