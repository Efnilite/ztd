package dev.efnilite.ztd.troop

import org.bukkit.entity.*
import java.util.*
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sqrt

enum class TroopType(

    /**
     * The entity type.
     */
    val type: EntityType,

    /**
     * The amount of health this type has.
     */
    val health: Int,

    /**
     * The movement speed in blocks per tick.
     */
    val speed: Double,

    /**
     * The amount to spawn per round. Round number is provided. Starts from 1.
     */
    val spawnAmount: (Int) -> Double,

    /**
     * The amount of ticks between mob spawns. Round number is provided. Starts from 1.
     */
    val spawnSpeed: (Int) -> Double,

    /**
     * What to do on spawn. Provided is the spawned entity.
     */
    val onSpawn: (LivingEntity) -> Unit,

    /**
     * Which troops to spawn on death.
     */
    val onDeath: () -> Map<TroopType, Int>,
) {

    ZOMBIE(
        EntityType.ZOMBIE, 1, 0.15,
        spawnAmount = { round -> -0.5 * (round - 10.5).pow(2) + 50 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity ->
            val zombie = entity as Zombie
            zombie.setShouldBurnInDay(false)
            zombie.setAdult()
        },
        onDeath = ::mapOf
    ),
    DROWNED(EntityType.DROWNED, 1, 0.2,
        spawnAmount = { round -> -0.7 * (round - 15.9).pow(2) + 100 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity ->
            val zombie = entity as Zombie
            zombie.setShouldBurnInDay(false)
            zombie.setAdult()
        },
        onDeath = { mapOf(ZOMBIE to 1) }),
    HUSK(EntityType.HUSK, 1, 0.16,
        spawnAmount = { round -> -0.3 * (round - 27.0).pow(2) + 120 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity ->
            val zombie = entity as Zombie
            zombie.setShouldBurnInDay(false)
            zombie.setAdult()
        },
        onDeath = { mapOf(DROWNED to 1) }),
    SKELETON(EntityType.SKELETON, 1, 0.25,
        spawnAmount = { round -> -0.8 * (round - 30.0).pow(2) + 150 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(HUSK to 1) }),
    STRAY(EntityType.STRAY, 1, 0.1,
        spawnAmount = { round -> -0.5 * (round - 37.0).pow(2) + 150 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(SKELETON to 1) }),
    WITHER_SKELETON(EntityType.WITHER_SKELETON, 1, 0.3,
        spawnAmount = { round -> 10 * sqrt(round - 22.99) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(STRAY to 1) }),
    WITCH(EntityType.WITCH, 1, 0.18,
        spawnAmount = { round -> 10 * sqrt(round - 21.99) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(WITHER_SKELETON to 1) }),
    CREEPER(EntityType.CREEPER, 1, 0.2,
        spawnAmount = { round -> 12 * sqrt(round - 25.0) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(WITCH to 1) }),
    CHARGED_CREEPER(EntityType.CREEPER, 1, 0.2,
        spawnAmount = { round -> 12 * sqrt(round - 27.0) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as Creeper).isPowered = true },
        onDeath = { mapOf(CREEPER to 1) }),
    VINDICATOR(EntityType.VINDICATOR, 1, 0.3,
        spawnAmount = { round -> 10 * sqrt(round - 35.9) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(CREEPER to 2,
            CHARGED_CREEPER to 2
        ) }),
    RAVAGER(EntityType.RAVAGER, 1000, 0.13,
        spawnAmount = { round -> 5 * sqrt(round - 44.9) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(WITCH to 5,
            CREEPER to 25,
            CHARGED_CREEPER to 10,
            VINDICATOR to 25
        ) }),
    WARDEN(EntityType.WARDEN, 2500, 0.15,
        spawnAmount = { round -> (round - 47.0).pow(2.1) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(
            CHARGED_CREEPER to 25,
            VINDICATOR to 25,
            RAVAGER to 10
        ) });

    companion object {

        private val troopToHealth: MutableMap<TroopType, Int> = EnumMap(TroopType::class.java)
        private val healthToTroop: MutableMap<Int, TroopType> = HashMap()

        init {
            var health = 0
            for (type in values()) {
                health += type.health

                TroopType.healthToTroop[health] = type
                TroopType.troopToHealth[type] = health
            }
        }

        fun getTroopFromHealth(totalHealth: Int): TroopType? = TroopType.healthToTroop[totalHealth]
        fun getHealthFromTroop(type: TroopType): Int = TroopType.troopToHealth[type]!!

        fun getNext(type: TroopType): TroopType {
            val values = values()
            return values[values.indexOf(type) + 1]
        }
    }
}