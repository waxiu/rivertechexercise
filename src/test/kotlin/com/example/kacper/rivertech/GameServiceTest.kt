import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.game.GameLogic
import com.example.rivertech.game.GameLogicFactory
import com.example.rivertech.game.enums.GameType
import com.example.rivertech.model.Bet
import com.example.rivertech.model.Player
import com.example.rivertech.model.Transaction
import com.example.rivertech.model.Wallet
import com.example.rivertech.model.enums.BetStatus
import com.example.rivertech.repository.PlayerRepository
import com.example.rivertech.service.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.*

class GameServiceTest {

    private val gameLogicFactory = mock(GameLogicFactory::class.java)
    private val playerRepository = mock(PlayerRepository::class.java)
    private val walletService = mock(WalletService::class.java)
    private val betService = mock(BetService::class.java)
    private val transactionService = mock(TransactionService::class.java)
    private val gameService = GameService(gameLogicFactory, playerRepository, walletService, betService, transactionService)

    @Test
    fun `should play game successfully`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal(100)
        val chosenNumber = 5
        val gameType = GameType.ODDS_BASED
        val player = Player(id = playerId, wallet = Wallet(id = 1L, balance = BigDecimal(200)))
        val transaction = Transaction(id = 1L, amount = betAmount)
        val bet = Bet(id = 1L, betAmount = betAmount, betNumber = chosenNumber, status = BetStatus.PENDING)
        val gameLogic = mock(GameLogic::class.java)

        `when`(playerRepository.findById(playerId)).thenReturn(Optional.of(player))
        `when`(gameLogicFactory.getGameLogic(gameType)).thenReturn(gameLogic)
        `when`(gameLogic.calculateWinnings(anyInt(), eq(chosenNumber), eq(betAmount))).thenReturn(BigDecimal(200))
        `when`(betService.createPendingBet(player, betAmount, chosenNumber, transaction)).thenReturn(bet)

        // Act
        val result = gameService.playGame(playerId, betAmount, chosenNumber, gameType)

        // Assert
        assertNotNull(result)
        assertEquals(BigDecimal(200), result.winnings)
        verify(walletService).deductFundsFromWallet(player.wallet!!, betAmount)
        verify(walletService).addFundsToWallet(player.wallet!!, result.winnings)
        verify(transactionService).createBetTransaction(player.wallet!!, betAmount)
        verify(transactionService).updateWalletAndTransactions(player.wallet!!, result.winnings)
        verify(betService).finalizeBet(bet, result)
    }

    @Test
    fun `should throw exception when player not found`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal(100)
        val chosenNumber = 5
        val gameType = GameType.ODDS_BASED

        `when`(playerRepository.findById(playerId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            gameService.playGame(playerId, betAmount, chosenNumber, gameType)
        }

        assertEquals("Player not found", exception.message)
    }

    @Test
    fun `should throw exception when insufficient funds`() {
        // Arrange
        val playerId = 1L
        val betAmount = BigDecimal(100)
        val chosenNumber = 5
        val gameType = GameType.ODDS_BASED
        val player = Player(id = playerId, wallet = Wallet(id = 1L, balance = BigDecimal(50)))

        `when`(playerRepository.findById(playerId)).thenReturn(Optional.of(player))

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            gameService.playGame(playerId, betAmount, chosenNumber, gameType)
        }

        assertEquals("Insufficient funds", exception.message)
    }

    @Test
    fun `should generate random number`() {
        // Act
        val randomNumber = gameService.generateRandomNumber()

        // Assert
        assertTrue(randomNumber in 1..10)
    }
}
