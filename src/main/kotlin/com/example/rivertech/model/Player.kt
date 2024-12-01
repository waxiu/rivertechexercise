package com.example.rivertech.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "players")
data class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var name: String = "",

    var surname: String = "",

    @Column(unique = true, nullable = false)
    var username: String = "",

    @OneToOne(mappedBy = "player", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var wallet: Wallet? = null,

    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var bets: MutableList<Bet> = mutableListOf()

) {
    constructor(name: String, surname: String, username: String) : this(
        name = name,
        surname = surname,
        username = username,
        wallet = Wallet()
    )
}
