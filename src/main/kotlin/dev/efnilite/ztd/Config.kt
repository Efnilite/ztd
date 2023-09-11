package dev.efnilite.ztd

import dev.efnilite.vilib.lib.configupdater.configupdater.ConfigUpdater
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Config management class.
 */
enum class Config(
    /**
     * The name of this file, e.g. config.yml
     */
    private val fileName: String,

    /**
     * The sections in the file that will be ignored when updating the keys.
     */
    private val ignoredSections: List<String>,
) {
    CONFIG("config.yml", emptyList()),
    ARCHER("towers/archer.yml", emptyList()),
    ICE("towers/ice.yml", emptyList()),
    MAGE("towers/mage.yml", emptyList()),
    ROCK("towers/rock.yml", emptyList());

    /**
     * The [FileConfiguration] instance associated with this config file.
     */
    private lateinit var fileConfiguration: FileConfiguration

    /**
     * The path to this file, incl. plugin folder.
     */
    private val path: File = ZTD.getInFolder(fileName)

    init {
        if (!path.exists()) {
            ZTD.instance.saveResource(fileName, false)
            ZTD.logging.info("Created config file $fileName")
        }
        update()
        load()
    }

    /**
     * Loads the file from disk.
     */
    fun load() {
        fileConfiguration = YamlConfiguration.loadConfiguration(path)
    }

    /**
     * Updates the file so all keys are present.
     */
    fun update() {
        try {
            ConfigUpdater.update(ZTD.instance, fileName, path, ignoredSections)
        } catch (ex: Exception) {
            ZTD.logging.stack("Error while trying to update config file", ex)
        }
    }

    /**
     * @param path The path.
     * @return True when path exists, false if not.
     */
    fun isPath(path: String): Boolean {
        return fileConfiguration.isSet(path)
    }

    /**
     * @param path The path.
     * @return The value at path.
     */
    fun get(path: String): Any {
        check(path)

        return fileConfiguration.get(path) ?: ""
    }

    /**
     * @param path The path.
     * @return The boolean value at path.
     */
    fun getBoolean(path: String): Boolean {
        check(path)

        return fileConfiguration.getBoolean(path)
    }

    /**
     * @param path The path.
     * @return The int value at path.
     */
    fun getInt(path: String): Int {
        check(path)

        return fileConfiguration.getInt(path)
    }

    /**
     * @param path The path.
     * @return The double value at path.
     */
    fun getDouble(path: String): Double {
        check(path)

        return fileConfiguration.getDouble(path)
    }

    /**
     * @param path The path.
     * @return The String value at path.
     */
    fun getString(path: String): String {
        check(path)

        return fileConfiguration.getString(path, "") ?: ""
    }

    /**
     * @param path The path.
     * @return The String list value at path.
     */
    fun getStringList(path: String): List<String> {
        check(path)

        return fileConfiguration.getStringList(path)
    }

    /**
     * @param path The path.
     * @return The int list value at path.
     */
    fun getIntList(path: String): List<Int> {
        check(path)

        return fileConfiguration.getIntegerList(path)
    }

    /**
     * @param path The path.
     * @param deep Whether search should include children of children as well.
     * @return The children nodes from path.
     */
    fun getChildren(path: String, deep: Boolean = false): List<String> {
        check(path)

        val section: ConfigurationSection = fileConfiguration.getConfigurationSection(path) ?: return ArrayList()

        return ArrayList<String>(section.getKeys(deep))
    }

    // checks if the specified path exists to avoid developer error
    private fun check(path: String) {
        if (!isPath(path)) throw NoSuchElementException("Unknown path $path in $fileName")
    }

    companion object {
        /**
         * Reloads all config files.
         */
        fun reload() {
            values().forEach(dev.efnilite.ztd.Config::load)

            ZTD.logging.info("Loaded all config files")
        }
    }
}
