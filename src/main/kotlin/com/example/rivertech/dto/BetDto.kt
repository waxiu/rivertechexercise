import com.example.rivertech.game.enums.GameType
import com.example.rivertech.model.enums.BetStatus
import java.math.BigDecimal

data class BetDto(
    val id: Long,
    val betAmount: BigDecimal,
    val betNumber: Int,
    val status: BetStatus,
    val playerId: Long?
)