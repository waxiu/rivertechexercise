package com.example.rivertech.service

import com.example.rivertech.model.Wallet
import com.example.rivertech.repository.WalletRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
class WalletServiceTest {

    private val walletRepository: WalletRepository = mock(WalletRepository::class.java)
    private val walletService = WalletService(walletRepository)

    @Test
    fun `should deduct funds from wallet`() {
        val wallet = Wallet(balance = BigDecimal(100), totalWinnings = BigDecimal(50))
        val betAmount = BigDecimal(30)

        walletService.deductFundsFromWallet(wallet, betAmount)

        assertEquals(BigDecimal(70), wallet.balance)
        assertEquals(BigDecimal(20), wallet.totalWinnings)
        verify(walletRepository, times(1)).save(wallet)
    }

    @Test
    fun `should add winnings to wallet`() {
        val wallet = Wallet(balance = BigDecimal(100), totalWinnings = BigDecimal(50))
        val winnings = BigDecimal(40)

        walletService.addFundsToWallet(wallet, winnings)

        assertEquals(BigDecimal(140), wallet.balance)
        assertEquals(BigDecimal(90), wallet.totalWinnings)
        verify(walletRepository, times(1)).save(wallet)
    }

    @Test
    fun `should deposit funds to wallet`() {
        val wallet = Wallet(balance = BigDecimal(100))
        val depositAmount = BigDecimal(50)

        walletService.depositFunds(wallet, depositAmount)

        assertEquals(BigDecimal(150), wallet.balance)
        verify(walletRepository, times(1)).save(wallet)
    }

    @Test
    fun `should throw exception for zero or negative deposit amount`() {
        val wallet = Wallet(balance = BigDecimal(100))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            walletService.depositFunds(wallet, BigDecimal.ZERO)
        }

        assertEquals("Deposit amount must be greater than zero", exception.message)
        verify(walletRepository, never()).save(wallet)
    }
}
