package com.example.rivertech.service

import com.example.rivertech.dto.PlayerRegistrationDto
import com.example.rivertech.model.Player
import com.example.rivertech.model.Wallet
import com.example.rivertech.repository.PlayerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val walletService: WalletService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PlayerService::class.java)
    }

    fun registerPlayer(dto: PlayerRegistrationDto): Player {
        logger.info("Registering new player with username: {}", dto.username)

        if (playerRepository.findByUsername(dto.username).isPresent) {
            logger.warn("Username '{}' already exists", dto.username)
            throw IllegalArgumentException("Username '${dto.username}' is already taken.")
        }

        val player = Player(
            name = dto.name,
            surname = dto.surname,
            username = dto.username
        )

        // Tworzenie portfela i ustawienie relacji dwustronnej
        val wallet = Wallet(balance = BigDecimal(1000), totalWinnings = BigDecimal.ZERO)
        wallet.player = player
        player.wallet = wallet

        val savedPlayer = playerRepository.save(player) // Zapis automatycznie zapisze też wallet
        logger.info("Player registered successfully with playerId: {}", savedPlayer.id)

        return savedPlayer
    }


    fun depositToPlayerWallet(playerId: Long, amount: BigDecimal) {
        logger.info("Starting deposit for playerId: {} with amount: {}", playerId, amount)

        val player = playerRepository.findById(playerId)
            .orElseThrow {
                logger.error("Player with ID {} not found", playerId)
                IllegalArgumentException("Player not found with ID: $playerId")
            }

        val wallet = player.wallet ?: throw IllegalStateException("Wallet not found for playerId: $playerId")
        walletService.depositFunds(wallet, amount)

        logger.info("Deposit to playerId: {} completed successfully", playerId)
    }
}
