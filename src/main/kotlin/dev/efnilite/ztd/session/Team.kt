package dev.efnilite.ztd.session

enum class Team {

    SINGLE,
    RED,
    BLU;

    companion object {
        fun getOpposite(team: Team): Team {
            return when (team) {
                Team.SINGLE -> Team.SINGLE
                Team.RED -> Team.BLU
                Team.BLU -> Team.RED
            }
        }
    }

}