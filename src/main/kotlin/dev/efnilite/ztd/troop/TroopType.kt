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
    val onDeath: () -> Map<Int, TroopType>,
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
        onDeath = { mapOf(1 to ZOMBIE) }),
    HUSK(EntityType.HUSK, 1, 0.16,
        spawnAmount = { round -> -0.3 * (round - 27.0).pow(2) + 120 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity ->
            val zombie = entity as Zombie
            zombie.setShouldBurnInDay(false)
            zombie.setAdult()
        },
        onDeath = { mapOf(1 to DROWNED) }),
    SKELETON(EntityType.SKELETON, 1, 0.25,
        spawnAmount = { round -> -0.8 * (round - 30.0).pow(2) + 150 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(1 to HUSK) }),
    STRAY(EntityType.STRAY, 1, 0.1,
        spawnAmount = { round -> -0.5 * (round - 37.0).pow(2) + 150 },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(1 to SKELETON) }),
    WITHER_SKELETON(EntityType.WITHER_SKELETON, 1, 0.3,
        spawnAmount = { round -> 10 * sqrt(round - 22.99) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as AbstractSkeleton).setShouldBurnInDay(false) },
        onDeath = { mapOf(1 to STRAY) }),
    WITCH(EntityType.WITCH, 1, 0.18,
        spawnAmount = { round -> 10 * sqrt(round - 21.99) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(1 to WITHER_SKELETON) }),
    CREEPER(EntityType.CREEPER, 1, 0.2,
        spawnAmount = { round -> 12 * sqrt(round - 25.0) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(1 to WITCH) }),
    CHARGED_CREEPER(EntityType.CREEPER, 1, 0.2,
        spawnAmount = { round -> 12 * sqrt(round - 27.0) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { entity -> (entity as Creeper).isPowered = true },
        onDeath = { mapOf(1 to CREEPER) }),
    VINDICATOR(EntityType.VINDICATOR, 1, 0.3,
        spawnAmount = { round -> 10 * sqrt(round - 35.9) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(2 to CREEPER,
            2 to CHARGED_CREEPER
        ) }),
    RAVAGER(EntityType.RAVAGER, 1000, 0.13,
        spawnAmount = { round -> 5 * sqrt(round - 44.9) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(5 to WITCH,
            25 to CREEPER,
            10 to CHARGED_CREEPER,
            25 to VINDICATOR
        ) }),
    WARDEN(EntityType.WARDEN, 2500, 0.15,
        spawnAmount = { round -> (round - 47.0).pow(2.1) },
        spawnSpeed = { round -> 1.0 / log(round + 0.5, 1000.0)},
        onSpawn = { },
        onDeath = { mapOf(
            25 to CHARGED_CREEPER,
            25 to VINDICATOR,
            10 to RAVAGER
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