package com.example.rivertech.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "wallets")
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var balance: BigDecimal = BigDecimal.ZERO,

    var totalWinnings: BigDecimal = BigDecimal.ZERO,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @JsonIgnore
    var player: Player? = null,

    @OneToMany(mappedBy = "wallet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var transactions: MutableList<Transaction> = mutableListOf()

) {
    constructor(balance: BigDecimal, totalWinnings: BigDecimal) : this() {
        this.balance = balance
        this.totalWinnings = totalWinnings
    }

    fun deductBetAmount(betAmount: BigDecimal) {
        this.balance = this.balance.subtract(betAmount)
    }

    fun addWinnings(winnings: BigDecimal?) {
        this.balance = this.balance.add(winnings)
    }
}
