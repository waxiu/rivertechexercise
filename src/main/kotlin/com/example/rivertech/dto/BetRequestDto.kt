package com.example.rivertech.dto

import com.example.rivertech.game.enums.GameType
import java.math.BigDecimal

data class BetRequestDto(
    var playerId: Long? = null,
    var betAmount: BigDecimal = BigDecimal.ZERO,
    var betNumber: Int = 0,
    var gameType: GameType
)
