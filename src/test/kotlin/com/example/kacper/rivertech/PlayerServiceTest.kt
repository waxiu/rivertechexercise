package com.example.rivertech.service

import com.example.rivertech.dto.PlayerRegistrationDto
import com.example.rivertech.model.Player
import com.example.rivertech.model.Wallet
import com.example.rivertech.repository.PlayerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*

class PlayerServiceTest {

    private val playerRepository = mock(PlayerRepository::class.java)
    private val walletService = mock(WalletService::class.java)
    private val playerService = PlayerService(playerRepository, walletService)

    @Test
    fun `should register new player successfully`() {
        val dto = PlayerRegistrationDto("John", "Doe", "johndoe")
        val savedPlayer = Player(1L, "John", "Doe", "johndoe", Wallet(BigDecimal(1000), BigDecimal.ZERO))

        `when`(playerRepository.findByUsername(dto.username)).thenReturn(Optional.empty())
        `when`(playerRepository.save(any<Player>())).thenReturn(savedPlayer)

        val result = playerService.registerPlayer(dto)

        assertEquals("johndoe", result.username)
        assertEquals(BigDecimal(1000), result.wallet?.balance)
        verify(playerRepository).save(any<Player>())
    }

    @Test
    fun `should throw exception when username already exists`() {
        val dto = PlayerRegistrationDto("John", "Doe", "johndoe")

        `when`(playerRepository.findByUsername(dto.username)).thenReturn(Optional.of(Player()))

        val exception = assertThrows<IllegalArgumentException> {
            playerService.registerPlayer(dto)
        }

        assertEquals("Username 'johndoe' is already taken.", exception.message)
        verify(playerRepository, never()).save(any<Player>())
    }

    @Test
    fun `should deposit to player's wallet successfully`() {
        val playerId = 1L
        val depositAmount = BigDecimal(500)
        val wallet = Wallet(BigDecimal(1000), BigDecimal.ZERO)
        val player = Player(1L, "John", "Doe", "johndoe", wallet)

        `when`(playerRepository.findById(any<Long>())).thenReturn(Optional.of(player))

        playerService.depositToPlayerWallet(playerId, depositAmount)

        verify(walletService).depositFunds(wallet, depositAmount)
        verify(playerRepository).findById(playerId)
    }

    @Test
    fun `should throw exception when player not found during deposit`() {
        val playerId = 1L
        val depositAmount = BigDecimal(500)

        `when`(playerRepository.findById(any<Long>())).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            playerService.depositToPlayerWallet(playerId, depositAmount)
        }

        assertEquals("Player not found with ID: $playerId", exception.message)
        verifyNoInteractions(walletService)
    }

    @Test
    fun `should throw exception when wallet not found during deposit`() {
        val playerId = 1L
        val depositAmount = BigDecimal(500)
        val player = Player(1L, "John", "Doe", "johndoe", null)

        `when`(playerRepository.findById(any<Long>())).thenReturn(Optional.of(player))

        val exception = assertThrows<IllegalStateException> {
            playerService.depositToPlayerWallet(playerId, depositAmount)
        }

        assertEquals("Wallet not found for playerId: $playerId", exception.message)
        verifyNoInteractions(walletService)
    }
}
