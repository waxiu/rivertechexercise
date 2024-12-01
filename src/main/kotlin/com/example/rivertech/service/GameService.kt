package com.example.rivertech.service

import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.game.GameLogicFactory
import com.example.rivertech.game.enums.GameType
import com.example.rivertech.model.Bet
import com.example.rivertech.model.Player
import com.example.rivertech.model.Transaction
import com.example.rivertech.model.enums.BetStatus
import com.example.rivertech.model.enums.TransactionType
import com.example.rivertech.repository.PlayerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.random.Random

@Service
class GameService(
    private val gameLogicFactory: GameLogicFactory,
    private val playerRepository: PlayerRepository,
    private val walletService: WalletService,
    private val betService: BetService,
    private val transactionService: TransactionService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GameService::class.java)
    }

    fun playGame(playerId: Long, betAmount: BigDecimal, chosenNumber: Int, gameType: GameType): GameResultDto {
        logger.info("Game initiated for playerId: {}, betAmount: {}, chosenNumber: {}, gameType: {}",
            playerId, betAmount, chosenNumber, gameType)

        val player = validatePlayerAndBalance(playerId, betAmount)

        walletService.deductFundsFromWallet(player.wallet!!, betAmount)
        logger.info("Funds deducted from walletId: {} for amount: {}", player.wallet!!.id, betAmount)

        val transaction = transactionService.createBetTransaction(player.wallet!!, betAmount)
        logger.info("Transaction created for walletId: {}, amount: {}, type: {}",
            player.wallet!!.id, betAmount, TransactionType.BET)

        val bet = betService.createPendingBet(player, betAmount, chosenNumber, transaction)
        logger.info("Bet created with betId: {} for playerId: {}", bet.id, playerId)

        val randomNumber = generateRandomNumber()

        val gameResultDto = generateGameResult(gameType, bet, betAmount, chosenNumber, randomNumber)
        logger.info("Game result generated with randomNumber: {}, winnings: {}",
            gameResultDto.generatedNumber, gameResultDto.winnings)

        walletService.addFundsToWallet(player.wallet!!, gameResultDto.winnings)
        logger.info("Funds added to walletId: {} for winnings: {}", player.wallet!!.id, gameResultDto.winnings)

        transactionService.updateWalletAndTransactions(player.wallet!!, gameResultDto.winnings)
        logger.info("Transactions updated for walletId: {} with winnings: {}",
            player.wallet!!.id, gameResultDto.winnings)

        betService.finalizeBet(bet, gameResultDto)
        logger.info("Bet finalized with betId: {}, status: {}, winnings: {}",
            bet.id, BetStatus.COMPLETED, bet.winnings)

        logger.info("Game completed for playerId: {}, betId: {}", playerId, bet.id)
        return gameResultDto
    }

    private fun validatePlayerAndBalance(playerId: Long, betAmount: BigDecimal): Player {
        logger.debug("Validating playerId: {} and balance for betAmount: {}", playerId, betAmount)
        val player = playerRepository.findById(playerId)
            .orElseThrow {
                logger.error("Player with playerId: {} not found", playerId)
                RuntimeException("Player not found")
            }
        val wallet = player.wallet ?: throw RuntimeException("Wallet not found for playerId: $playerId")

        if (wallet.balance < betAmount) {
            logger.error("Insufficient funds for playerId: {}, walletId: {}, balance: {}, betAmount: {}",
                playerId, wallet.id, wallet.balance, betAmount)
            throw RuntimeException("Insufficient funds")
        }
        logger.debug("Player validated with playerId: {} and sufficient balance", playerId)
        return player
    }

    private fun generateGameResult(gameType: GameType, bet: Bet, betAmount: BigDecimal, chosenNumber: Int, randomNumber: Int): GameResultDto {
        logger.debug("Generating game result for betId: {}, gameType: {}", bet.id, gameType)
        val gameLogic = gameLogicFactory.getGameLogic(gameType)
        val winnings = gameLogic.calculateWinnings(randomNumber, chosenNumber, betAmount)

        bet.generatedNumber = randomNumber
        bet.winnings = winnings
        logger.debug("Game result generated for betId: {}, randomNumber: {}, winnings: {}",
            bet.id, randomNumber, winnings)
        return GameResultDto(randomNumber, winnings)
    }

    fun generateRandomNumber(): Int {
        val randomNumber = Random.nextInt(1, 11)
        logger.debug("Generated random number: {}", randomNumber)
        return randomNumber
    }
}
