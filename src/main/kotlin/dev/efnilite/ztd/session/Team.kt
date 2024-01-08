package dev.efnilite.ztd.session

enum class Team {

    SINGLE,
    RED,
    BLU;

    companion object {
        fun getOpposite(team: Team): Team {
            return when (team) {
                SINGLE -> SINGLE
                RED -> BLU
                BLU -> RED
            }
        }
    }

}