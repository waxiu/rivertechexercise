package com.example.rivertech.controller

import com.example.rivertech.dto.BetHistoryDto
import com.example.rivertech.dto.BetRequestDto
import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.dto.ApiResponse
import com.example.rivertech.service.BetService
import com.example.rivertech.service.GameService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bet")
class BetController(
    private val gameService: GameService,
    private val betService: BetService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BetController::class.java)
    }

    @PostMapping("/place")
    fun placeBet(@RequestBody betRequestDto: BetRequestDto): ResponseEntity<ApiResponse<GameResultDto>> {
        logger.info(
            "Placing a bet: playerId={}, betAmount={}, betNumber={}, gameType={}",
            betRequestDto.playerId, betRequestDto.betAmount, betRequestDto.betNumber, betRequestDto.gameType
        )

        val result = gameService.playGame(
            betRequestDto.playerId!!,
            betRequestDto.betAmount,
            betRequestDto.betNumber,
            betRequestDto.gameType
        )

        logger.info("Bet placed successfully: {}", result)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Bet placed successfully", data = result))
    }

    @GetMapping("/history/{playerId}")
    fun getBetHistory(
        @PathVariable playerId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<BetHistoryDto>> {
        logger.info("Requesting bet history: playerId={}, page={}, size={}", playerId, page, size)

        val pageable: Pageable = PageRequest.of(page, size)
        val betHistory = betService.getBetHistoryForPlayer(playerId, pageable)

        logger.info("Bet history retrieved successfully for playerId: {}", playerId)
        return ResponseEntity.ok(betHistory)
    }
}
