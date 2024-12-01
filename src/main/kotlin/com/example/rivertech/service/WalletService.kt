package com.example.rivertech.service

import com.example.rivertech.model.Player
import com.example.rivertech.model.Wallet
import com.example.rivertech.repository.WalletRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WalletService::class.java)
    }

    fun deductFundsFromWallet(wallet: Wallet, betAmount: BigDecimal) {
        logger.info("Deducting funds from walletId: {}, betAmount: {}", wallet.id, betAmount)

        wallet.deductBetAmount(betAmount)
        wallet.totalWinnings = wallet.totalWinnings.subtract(betAmount)
        walletRepository.save(wallet)

        logger.info("Funds deducted successfully. WalletId: {}, new balance: {}, total winnings: {}",
            wallet.id, wallet.balance, wallet.totalWinnings)
    }

    fun addFundsToWallet(wallet: Wallet, winnings: BigDecimal) {
        logger.info("Adding winnings to walletId: {}, amount: {}", wallet.id, winnings)

        wallet.addWinnings(winnings)
        wallet.totalWinnings = wallet.totalWinnings.add(winnings)
        walletRepository.save(wallet)

        logger.info("Winnings added successfully. WalletId: {}, new balance: {}, total winnings: {}",
            wallet.id, wallet.balance, wallet.totalWinnings)
    }

    fun depositFunds(wallet: Wallet, amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Deposit amount must be greater than zero" }

        logger.info("Depositing amount: {} to walletId: {}", amount, wallet.id)

        wallet.balance = wallet.balance.add(amount)
        walletRepository.save(wallet)

        logger.info("Deposit successful. New balance for walletId: {} is {}", wallet.id, wallet.balance)
    }
}
