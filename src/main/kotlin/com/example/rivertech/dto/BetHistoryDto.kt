package com.example.rivertech.dto

import com.example.rivertech.model.enums.BetStatus
import java.math.BigDecimal

data class BetHistoryDto(
    val betAmount: BigDecimal?,
    val betNumber: Int?,
    val generatedNumber: Int?,
    val winnings: BigDecimal?,
    val status: BetStatus?
)
