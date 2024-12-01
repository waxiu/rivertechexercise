package com.example.rivertech.service

import BetDto
import com.example.rivertech.dto.BetHistoryDto
import com.example.rivertech.dto.GameResultDto
import com.example.rivertech.model.Bet
import com.example.rivertech.model.Player
import com.example.rivertech.model.Transaction
import com.example.rivertech.model.enums.BetStatus
import com.example.rivertech.repository.BetRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
 class BetService(
    private val betRepository: BetRepository
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BetService::class.java)
    }

    @Transactional
     fun createPendingBet(player: Player, betAmount: BigDecimal, chosenNumber: Int, transaction: Transaction): Bet {
        require(betAmount > BigDecimal.ZERO) {
            "Bet amount must be greater than zero."
        }

        logger.info("Creating pending bet for playerId: {}, betAmount: {}, chosenNumber: {}",
            player.id, betAmount, chosenNumber)

        val bet = Bet(
            player = player,
            betAmount = betAmount,
            betNumber = chosenNumber,
            status = BetStatus.PENDING,
            transaction = transaction
        )

        val savedBet = betRepository.save(bet)
        logger.info("Pending bet created with betId: {}, playerId: {}", savedBet.id, player.id)
        return savedBet
    }

    @Transactional
     fun finalizeBet(bet: Bet, gameResultDto: GameResultDto) {
        logger.info("Finalizing bet with betId: {}, for playerId: {}", bet.id, bet.player?.id)

        check(bet.status == BetStatus.PENDING) {
            "Bet can only be finalized if it is in a pending state."
        }

        bet.apply {
            status = BetStatus.COMPLETED
            generatedNumber = gameResultDto.generatedNumber
            winnings = gameResultDto.winnings
        }

        val updatedBet = betRepository.save(bet)
        logger.info("Bet finalized with betId: {}, status: {}, winnings: {}",
            updatedBet.id, updatedBet.status, updatedBet.winnings)
    }

    fun getBetHistoryForPlayer(playerId: Long, pageable: Pageable): Page<BetHistoryDto> {
        logger.info("Fetching paginated bet history for playerId: {}, page: {}, size: {}",
            playerId, pageable.pageNumber, pageable.pageSize)

        return betRepository.findByPlayerId(playerId, pageable)
            .map { bet ->
                BetHistoryDto(
                    betAmount = bet.betAmount,
                    betNumber = bet.betNumber,
                    generatedNumber = bet.generatedNumber,
                    winnings = bet.winnings,
                    status = bet.status
                )
            }
    }
}
