package com.example.rivertech.game

import java.math.BigDecimal
import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class CrazyHouseLogic : GameLogic {

    override fun calculateWinnings(randomNumber: Int, chosenNumber: Int, betAmount: BigDecimal): BigDecimal {
        val difference = abs(randomNumber - chosenNumber)
        return if (difference == 0) {
            betAmount.multiply(BigDecimal.valueOf(20))
        } else {
            BigDecimal.ZERO
        }
    }
}
