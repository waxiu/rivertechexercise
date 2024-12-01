package com.example.rivertech.service

import com.example.rivertech.repository.PlayerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class LeaderboardService(
    private val redisTemplate: RedisTemplate<String, Long>,
    private val playerRepository: PlayerRepository
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(LeaderboardService::class.java)
        private const val REDIS_LEADERBOARD_KEY = "leaderboard"
    }

    fun getTopPlayers(top: Int): Set<ZSetOperations.TypedTuple<Long>>? {
        logger.info("Fetching top {} players from leaderboard", top)
        val topPlayers = redisTemplate.opsForZSet()
            .reverseRangeWithScores(REDIS_LEADERBOARD_KEY, 0, maxOf(0L, top - 1L))
        if (topPlayers.isNullOrEmpty()) {
            logger.warn("No players found in the leaderboard")
        } else {
            logger.info("Fetched {} players from leaderboard", topPlayers.size)
        }
        return topPlayers
    }

    @Scheduled(fixedRate = 10000)
    fun updateLeaderboardInBatch() {
        logger.info("Starting leaderboard update")

        val players = playerRepository.findAll()
        logger.debug("Fetched {} players from the database", players.size)

        redisTemplate.delete(REDIS_LEADERBOARD_KEY)
        logger.debug("Cleared old leaderboard from Redis")

        players.forEach { player ->
            val totalWinnings = player.wallet?.totalWinnings ?: BigDecimal.ZERO
            redisTemplate.opsForZSet().add(REDIS_LEADERBOARD_KEY, player.id, totalWinnings.toDouble())
            logger.debug("Updated playerId: {} with totalWinnings: {} in leaderboard", player.id, totalWinnings)
        }

        logger.info("Leaderboard update completed")
    }
}
