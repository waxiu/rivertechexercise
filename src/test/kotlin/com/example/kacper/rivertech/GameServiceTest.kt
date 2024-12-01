package com.example.rivertech

import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.game.GameLogic
import com.example.rivertech.game.GameLogicFactory
import com.example.rivertech.game.enums.GameType
import com.example.rivertech.model.*
import com.example.rivertech.repository.PlayerRepository
import com.example.rivertech.service.BetService
import com.example.rivertech.service.GameService
import com.example.rivertech.service.TransactionService
import com.example.rivertech.service.WalletService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional
import org.mockito.kotlin.*

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

@ExtendWith(MockitoExtension::class)
class GameServiceTest {

    @Mock
    private lateinit var gameLogicFactory: GameLogicFactory

    @Mock
    private lateinit var playerRepository: PlayerRepository

    @Mock
    private lateinit var walletService: WalletService

    @Mock
    private lateinit var betService: BetService

    @Mock
    private lateinit var transactionService: TransactionService

    @InjectMocks
    private lateinit var gameService: GameService

    @Test
    fun `playGame should deduct funds and add winnings when player wins`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal("100")
        val chosenNumber = 5
        val gameType = GameType.ODDS_BASED
        val generatedNumber = 5 // Expected random number
        val winnings = BigDecimal("1000")

        val player = Player()
        val wallet = Wallet(BigDecimal("1000"), BigDecimal("0"))
        player.wallet = wallet

        val transaction = Transaction()
        val bet = Bet()
        val gameLogic = mock<GameLogic>()

        whenever(playerRepository.findById(playerId)).thenReturn(Optional.of(player))
        whenever(gameLogicFactory.getGameLogic(gameType)).thenReturn(gameLogic)
        whenever(gameLogic.calculateWinnings(generatedNumber, chosenNumber, betAmount)).thenReturn(winnings)
        whenever(transactionService.createBetTransaction(wallet, betAmount)).thenReturn(transaction)
        whenever(betService.createPendingBet(player, betAmount, chosenNumber, transaction)).thenReturn(bet)

        // Mocking random number
        val gameServiceSpy = spy(gameService)
        doReturn(generatedNumber).whenever(gameServiceSpy).generateRandomNumber()

        // Act
        val result = gameServiceSpy.playGame(playerId, betAmount, chosenNumber, gameType)

        // Assert
        assertThat(result.generatedNumber).isEqualTo(generatedNumber)
        assertThat(result.winnings).isEqualByComparingTo(winnings)

        verify(walletService).deductFundsFromWallet(wallet, betAmount)
        verify(walletService).addFundsToWallet(wallet, winnings)
        verify(transactionService).updateWalletAndTransactions(wallet, winnings)
        verify(betService).finalizeBet(bet, result)
    }

    @Test
    fun `playGame should throw exception when player not found`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal("100")
        val chosenNumber = 5
        val gameType = GameType.ODDS_BASED

        whenever(playerRepository.findById(playerId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThatThrownBy {
            gameService.playGame(playerId, betAmount, chosenNumber, gameType)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Player not found")

        verifyNoInteractions(walletService, transactionService, betService)
    }

    @Test
    fun `playGame should throw exception when insufficient funds`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal("1000")
        val player = Player()
        val wallet = Wallet(BigDecimal("500"), BigDecimal("0"))
        player.wallet = wallet

        whenever(playerRepository.findById(playerId)).thenReturn(Optional.of(player))

        // Act & Assert
        assertThatThrownBy {
            gameService.playGame(playerId, betAmount, 5, GameType.ODDS_BASED)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Insufficient funds")

        verifyNoInteractions(walletService, transactionService, betService)
    }
}
