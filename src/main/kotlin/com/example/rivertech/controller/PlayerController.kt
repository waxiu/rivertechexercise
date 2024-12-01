package com.example.rivertech.controller

import com.example.rivertech.dto.ApiResponse
import com.example.rivertech.dto.DepositRequestDto
import com.example.rivertech.dto.PlayerRegistrationDto
import com.example.rivertech.model.Player
import com.example.rivertech.model.Transaction
import com.example.rivertech.service.PlayerService
import com.example.rivertech.service.TransactionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/player")
class PlayerController(
    private val playerService: PlayerService,
    private val transactionService: TransactionService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PlayerController::class.java)
    }

    @PostMapping("/register")
    fun registerPlayer(@RequestBody dto: PlayerRegistrationDto): ResponseEntity<ApiResponse<Player>> {
        val savedPlayer = playerService.registerPlayer(dto)
        return ResponseEntity
            .status(201)
            .body(ApiResponse(success = true, message = "Player registered successfully", data = savedPlayer))
    }

    @GetMapping("/transactions/{playerId}")
    fun getTransactionsForPlayer(
        @PathVariable playerId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<Page<Transaction>>> {
        logger.info("Request for paginated transactions for playerId: {}, page: {}, size: {}", playerId, page, size)

        val pageable = PageRequest.of(page, size)
        val transactions = transactionService.getTransactionsForPlayer(playerId, pageable)

        return ResponseEntity.ok(ApiResponse(success = true, message = "Transactions fetched successfully", data = transactions))
    }

    @PostMapping("/deposit/{playerId}")
    fun depositToWallet(
        @PathVariable playerId: Long,
        @RequestBody depositRequestDto: DepositRequestDto
    ): ResponseEntity<ApiResponse<String>> {
        playerService.depositToPlayerWallet(playerId, depositRequestDto.amount)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Deposit successful", data = "Deposit successful"))
    }
}
