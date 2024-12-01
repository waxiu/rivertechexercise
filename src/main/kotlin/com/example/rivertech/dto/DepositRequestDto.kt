package com.example.rivertech.dto

import java.math.BigDecimal

data class DepositRequestDto(
    var amount: BigDecimal = BigDecimal.ZERO
)
