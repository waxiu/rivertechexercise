package com.example.rivertech.service

import com.example.rivertech.model.Transaction
import com.example.rivertech.model.Wallet
import com.example.rivertech.model.enums.TransactionType
import com.example.rivertech.repository.PlayerRepository
import com.example.rivertech.repository.TransactionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val playerRepository: PlayerRepository
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TransactionService::class.java)
    }

    fun getTransactionsForPlayer(playerId: Long, pageable: Pageable): Page<Transaction> {
        logger.info("Fetching paginated transactions for playerId: {}, page: {}, size: {}",
            playerId, pageable.pageNumber, pageable.pageSize)

        if (!playerRepository.existsById(playerId)) {
            logger.error("Player with ID {} not found", playerId)
            throw IllegalArgumentException("Player with ID $playerId not found.")
        }

        val transactions = transactionRepository.findByPlayerId(playerId, pageable)
        logger.info("Found {} transactions for playerId: {} on page: {}",
            transactions.totalElements, playerId, pageable.pageNumber)
        return transactions
    }

    fun createBetTransaction(wallet: Wallet, betAmount: BigDecimal): Transaction {
        logger.info("Creating bet transaction for walletId: {}, amount: {}", wallet.id, betAmount)

        val betTransaction = Transaction(
            type = TransactionType.BET,
            amount = betAmount.negate(),
            wallet = wallet
        )
        transactionRepository.save(betTransaction)
        logger.info("Bet transaction created with amount: {} for walletId: {}", betAmount.negate(), wallet.id)
        return betTransaction
    }

    fun updateWalletAndTransactions(wallet: Wallet, winnings: BigDecimal) {
        logger.info("Updating walletId: {} with winnings: {}", wallet.id, winnings)

        if (winnings > BigDecimal.ZERO) {
            val winTransaction = Transaction(
                type = TransactionType.WIN,
                amount = winnings,
                wallet = wallet
            )
            transactionRepository.save(winTransaction)
            logger.info("Win transaction created with amount: {} for walletId: {}", winnings, wallet.id)
        } else {
            logger.warn("No winnings to update for walletId: {}", wallet.id)
        }
    }
}
