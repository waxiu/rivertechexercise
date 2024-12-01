package com.example.rivertech.game

import com.example.rivertech.game.enums.GameType
import org.springframework.stereotype.Component

@Component
class GameLogicFactory(
    private val simpleGameLogic: SimpleGameLogic,
    private val crazyHouseLogic: CrazyHouseLogic
) {
    fun getGameLogic(gameType: GameType): GameLogic {
        return when (gameType) {
            GameType.ODDS_BASED -> simpleGameLogic
            GameType.CRAZY_HOUSE -> crazyHouseLogic
        }
    }
}
