package com.example.rivertech.repository

import com.example.rivertech.model.Bet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BetRepository : JpaRepository<Bet, Long> {
    fun findAllByPlayerId(playerId: Long): List<Bet>
    fun findByPlayerId(playerId: Long, pageable: Pageable): Page<Bet>
}
