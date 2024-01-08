package dev.efnilite.ztd.session

import org.jetbrains.annotations.Contract

enum class Team {

    SINGLE,
    RED,
    BLU;

    @Contract(pure = true)
    fun getOpposite(): Team {
        return when (this) {
            SINGLE -> SINGLE
            RED -> BLU
            BLU -> RED
        }
    }
}