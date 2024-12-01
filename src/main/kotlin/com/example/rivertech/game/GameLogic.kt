package com.example.rivertech.game

import java.math.BigDecimal

interface GameLogic {
    fun calculateWinnings(randomNumber: Int, chosenNumber: Int, betAmount: BigDecimal): BigDecimal
}
