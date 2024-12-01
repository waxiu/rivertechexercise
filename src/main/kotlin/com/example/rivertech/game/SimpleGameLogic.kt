package com.example.rivertech.game

import java.math.BigDecimal
import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class SimpleGameLogic : GameLogic {

    override fun calculateWinnings(randomNumber: Int, chosenNumber: Int, betAmount: BigDecimal): BigDecimal {
        val difference = abs(randomNumber - chosenNumber)
        return when (difference) {
            0 -> betAmount.multiply(BigDecimal.valueOf(10))
            1 -> betAmount.multiply(BigDecimal.valueOf(5))
            2 -> betAmount.divide(BigDecimal.valueOf(2))
            else -> BigDecimal.ZERO
        }
    }
}
