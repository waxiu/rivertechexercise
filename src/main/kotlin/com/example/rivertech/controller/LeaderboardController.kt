package com.example.rivertech.controller

import com.example.rivertech.dto.ApiResponse
import com.example.rivertech.dto.PlayerRankingDto
import com.example.rivertech.service.LeaderboardService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/leaderboard")
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(LeaderboardController::class.java)
    }

    @GetMapping("/winners")
    fun getLeaderboard(@RequestParam(defaultValue = "10") top: Int): ApiResponse<List<PlayerRankingDto>> {
        logger.info("Fetching top {} players from the leaderboard", top)

        val leaderboard: Set<ZSetOperations.TypedTuple<Long>>? = leaderboardService.getTopPlayers(top)

        val rankings = leaderboard?.map { entry ->
            PlayerRankingDto(
                playerId = entry.value ?: 0L,
                score = entry.score ?: 0.0
            )
        } ?: emptyList()

        logger.info("Leaderboard fetched successfully, returning {} entries", rankings.size)
        return ApiResponse(success = true, message = "Leaderboard fetched successfully", data = rankings)
    }
}
