package com.example.rivertech.repository

import com.example.rivertech.model.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PlayerRepository : JpaRepository<Player, Long> {
    fun findByUsername(username: String): Optional<Player>
}
