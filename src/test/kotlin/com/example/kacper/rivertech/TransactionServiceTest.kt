package com.example.rivertech.service

import com.example.rivertech.model.Transaction
import com.example.rivertech.model.Wallet
import com.example.rivertech.model.enums.TransactionType
import com.example.rivertech.repository.PlayerRepository
import com.example.rivertech.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.*

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mock(TransactionRepository::class.java)
    private val playerRepository: PlayerRepository = mock(PlayerRepository::class.java)
    private val transactionService = TransactionService(transactionRepository, playerRepository)

    @Test
    fun `should fetch paginated transactions for player`() {
        val playerId = 1L
        val pageable = PageRequest.of(0, 10)
        val transactions = listOf(
            Transaction(id = 1, type = TransactionType.BET, amount = BigDecimal.valueOf(-50)),
            Transaction(id = 2, type = TransactionType.WIN, amount = BigDecimal.valueOf(100))
        )
        val transactionPage: Page<Transaction> = PageImpl(transactions, pageable, transactions.size.toLong())

        `when`(playerRepository.existsById(playerId)).thenReturn(true)
        `when`(transactionRepository.findByPlayerId(playerId, pageable)).thenReturn(transactionPage)

        val result = transactionService.getTransactionsForPlayer(playerId, pageable)

        assertEquals(2, result.totalElements)
        verify(playerRepository, times(1)).existsById(playerId)
        verify(transactionRepository, times(1)).findByPlayerId(playerId, pageable)
    }

    @Test
    fun `should throw exception if player not found`() {
        val playerId = 1L
        val pageable = PageRequest.of(0, 10)

        `when`(playerRepository.existsById(playerId)).thenReturn(false)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            transactionService.getTransactionsForPlayer(playerId, pageable)
        }

        assertEquals("Player with ID $playerId not found.", exception.message)
        verify(playerRepository, times(1)).existsById(playerId)
        verify(transactionRepository, never()).findByPlayerId(playerId, pageable)
    }

    @Test
    fun `should create bet transaction`() {
        val wallet = Wallet(id = 1L, balance = BigDecimal.valueOf(100))
        val betAmount = BigDecimal.valueOf(50)

        val betTransaction = Transaction(
            id = 1,
            type = TransactionType.BET,
            amount = betAmount.negate(),
            wallet = wallet
        )

        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(betTransaction)

        val result = transactionService.createBetTransaction(wallet, betAmount)

        assertEquals(TransactionType.BET, result.type)
        assertEquals(betAmount.negate(), result.amount)
        assertEquals(wallet.id, result.wallet?.id)
        verify(transactionRepository, times(1)).save(any(Transaction::class.java))
    }

    @Test
    fun `should create win transaction when winnings are greater than zero`() {
        val wallet = Wallet(id = 1L)
        val winnings = BigDecimal.valueOf(100)

        val winTransaction = Transaction(
            id = 1,
            type = TransactionType.WIN,
            amount = winnings,
            wallet = wallet
        )

        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(winTransaction)

        transactionService.updateWalletAndTransactions(wallet, winnings)

        verify(transactionRepository, times(1)).save(any(Transaction::class.java))
    }

    @Test
    fun `should not create win transaction when winnings are zero`() {
        val wallet = Wallet(id = 1L)
        val winnings = BigDecimal.ZERO

        transactionService.updateWalletAndTransactions(wallet, winnings)

        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }
}
