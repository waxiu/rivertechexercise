package com.example.rivertech.model

import com.example.rivertech.model.enums.BetStatus
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "bets")
data class Bet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var betAmount: BigDecimal = BigDecimal.ZERO,

    var betNumber: Int = 0,

    var generatedNumber: Int = 0,

    var winnings: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    var status: BetStatus? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    var player: Player? = null,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "transaction_id")
    var transaction: Transaction? = null
)

