package com.example.rivertech.service

import com.example.rivertech.model.Player
import com.example.rivertech.model.Wallet
import com.example.rivertech.repository.PlayerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.math.BigDecimal
import java.util.*

class LeaderboardServiceTest {

    private val redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, Long>
    private val zSetOperations = mock(ZSetOperations::class.java) as ZSetOperations<String, Long>
    private val playerRepository = mock(PlayerRepository::class.java)

    private lateinit var leaderboardService: LeaderboardService

    @BeforeEach
    fun setUp() {
        `when`(redisTemplate.opsForZSet()).thenReturn(zSetOperations)
        leaderboardService = LeaderboardService(redisTemplate, playerRepository)
    }

    @Test
    fun `should fetch top players from leaderboard`() {
        // Arrange
        val topPlayers = setOf(
            mockTypedTuple(1L, 100.0),
            mockTypedTuple(2L, 80.0)
        )

        `when`(zSetOperations.reverseRangeWithScores("leaderboard", 0, 2))
            .thenReturn(topPlayers)

        // Act
        val result = leaderboardService.getTopPlayers(3)

        // Assert
        assertEquals(topPlayers, result)
        verify(zSetOperations).reverseRangeWithScores("leaderboard", 0, 2)
    }

    @Test
    fun `should return empty leaderboard if no players found`() {
        // Arrange
        `when`(zSetOperations.reverseRangeWithScores("leaderboard", 0, 2))
            .thenReturn(emptySet())

        // Act
        val result = leaderboardService.getTopPlayers(3)

        // Assert
        assertTrue(result.isNullOrEmpty())
        verify(zSetOperations).reverseRangeWithScores("leaderboard", 0, 2)
    }

    @Test
    fun `should update leaderboard in batch`() {
        // Arrange
        val players = listOf(
            Player(1L, "John", "Doe", "johndoe", Wallet(BigDecimal(1000), BigDecimal(500))),
            Player(2L, "Jane", "Smith", "janesmith", Wallet(BigDecimal(1500), BigDecimal(800)))
        )
        `when`(playerRepository.findAll()).thenReturn(players)

        // Act
        leaderboardService.updateLeaderboardInBatch()

        // Assert
        verify(redisTemplate).delete("leaderboard")
        verify(zSetOperations).add("leaderboard", 1L, 500.0)
        verify(zSetOperations).add("leaderboard", 2L, 800.0)
    }

    private fun mockTypedTuple(value: Long, score: Double): ZSetOperations.TypedTuple<Long> {
        val mock = mock(ZSetOperations.TypedTuple::class.java) as ZSetOperations.TypedTuple<Long>
        `when`(mock.value).thenReturn(value)
        `when`(mock.score).thenReturn(score)
        return mock
    }
}
