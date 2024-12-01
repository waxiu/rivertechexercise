package com.example.rivertech.model

import com.example.rivertech.model.enums.TransactionType
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Enumerated(EnumType.STRING)
    var type: TransactionType? = null,

    var amount: BigDecimal = BigDecimal.ZERO,

    @Temporal(TemporalType.TIMESTAMP)
    var timestamp: Date = Date(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    @JsonIgnore
    var wallet: Wallet? = null
)
