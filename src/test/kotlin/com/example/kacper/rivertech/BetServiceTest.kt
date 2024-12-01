package com.example.rivertech.service

import com.example.rivertech.dto.BetHistoryDto
import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.model.Bet
import com.example.rivertech.model.Player
import com.example.rivertech.model.Transaction
import com.example.rivertech.model.enums.BetStatus
import com.example.rivertech.model.enums.TransactionType
import com.example.rivertech.repository.BetRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

class BetServiceTest {

    private val betRepository = mock(BetRepository::class.java)
    private val betService = BetService(betRepository)

    @Test
    fun `should create pending bet successfully`() {
        // Arrange
        val player = Player(
            id = 1L,
            name = "John",
            surname = "Doe",
            username = "johndoe",
            wallet = null
        )
        val transaction = Transaction(
            id = 1L,
            type = TransactionType.BET,
            amount = BigDecimal(50)
        )
        val betAmount = BigDecimal(50)
        val chosenNumber = 7

        val bet = Bet(
            id = 1L,
            player = player,
            betAmount = betAmount,
            betNumber = chosenNumber,
            status = BetStatus.PENDING,
            transaction = transaction
        )
        `when`(betRepository.save(any(Bet::class.java))).thenReturn(bet)

        // Act
        val result = betService.createPendingBet(player, betAmount, chosenNumber, transaction)

        // Assert
        assertNotNull(result)
        assertEquals(BetStatus.PENDING, result.status)
        assertEquals(betAmount, result.betAmount)
        assertEquals(chosenNumber, result.betNumber)
        assertEquals(transaction, result.transaction)
        verify(betRepository).save(any(Bet::class.java))
    }

    @Test
    fun `should throw exception when bet amount is zero or less`() {
        // Arrange
        val player = Player(
            id = 1L,
            name = "John",
            surname = "Doe",
            username = "johndoe",
            wallet = null
        )
        val transaction = Transaction(
            id = 1L,
            type = TransactionType.BET,
            amount = BigDecimal.ZERO
        )
        val betAmount = BigDecimal.ZERO
        val chosenNumber = 7

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            betService.createPendingBet(player, betAmount, chosenNumber, transaction)
        }
        assertEquals("Bet amount must be greater than zero.", exception.message)
        verifyNoInteractions(betRepository)
    }

    @Test
    fun `should finalize bet successfully`() {
        // Arrange
        val player = Player(
            id = 1L,
            name = "John",
            surname = "Doe",
            username = "johndoe",
            wallet = null
        )
        val transaction = Transaction(
            id = 1L,
            type = TransactionType.BET,
            amount = BigDecimal(50)
        )
        val bet = Bet(
            id = 1L,
            player = player,
            betAmount = BigDecimal(50),
            betNumber = 7,
            status = BetStatus.PENDING,
            transaction = transaction
        )
        val gameResultDto = GameResultDto(
            generatedNumber = 7,
            winnings = BigDecimal(150)
        )

        `when`(betRepository.save(any(Bet::class.java))).thenReturn(bet)

        // Act
        betService.finalizeBet(bet, gameResultDto)

        // Assert
        assertEquals(BetStatus.COMPLETED, bet.status)
        assertEquals(7, bet.generatedNumber)
        assertEquals(BigDecimal(150), bet.winnings)
        verify(betRepository).save(any(Bet::class.java))
    }

    @Test
    fun `should throw exception when finalizing non-pending bet`() {
        // Arrange
        val bet = Bet(
            id = 1L,
            player = Player(1L, "John", "Doe", "johndoe", null),
            betAmount = BigDecimal(50),
            betNumber = 7,
            status = BetStatus.COMPLETED
        )
        val gameResultDto = GameResultDto(
            generatedNumber = 7,
            winnings = BigDecimal(150)
        )

        // Act & Assert
        val exception = assertThrows<IllegalStateException> {
            betService.finalizeBet(bet, gameResultDto)
        }
        assertEquals("Bet can only be finalized if it is in a pending state.", exception.message)
        verifyNoInteractions(betRepository)
    }

    @Test
    fun `should fetch bet history for player successfully`() {
        // Arrange
        val playerId = 1L
        val pageable = mock(Pageable::class.java)
        val bets = listOf(
            Bet(
                betAmount = BigDecimal(50),
                betNumber = 7,
                generatedNumber = 7,
                winnings = BigDecimal(150),
                status = BetStatus.COMPLETED
            )
        )
        val page = PageImpl(bets)
        `when`(betRepository.findByPlayerId(playerId, pageable)).thenReturn(page)

        // Act
        val result = betService.getBetHistoryForPlayer(playerId, pageable)

        // Assert
        assertNotNull(result)
        assertEquals(1, result.totalElements)
        assertEquals(BigDecimal(50), result.content[0].betAmount)
        verify(betRepository).findByPlayerId(playerId, pageable)
    }
}
