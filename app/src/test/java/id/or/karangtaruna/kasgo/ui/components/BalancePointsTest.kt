package id.or.karangtaruna.kasgo.ui.components

import id.or.karangtaruna.kasgo.data.models.LedgerType
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class BalancePointsTest {
    private fun date(day: Int, hour: Int = 12): Long = Calendar.getInstance().apply {
        clear(); set(2026, Calendar.SEPTEMBER, day, hour, 0, 0)
    }.timeInMillis
    private fun tx(day: Int, amount: Long, income: Boolean = true, hour: Int = 12) = TransactionItem(
        "$day-$hour-$amount", if (income) LedgerType.INCOME else LedgerType.EXPENSE,
        amount, "Test", "Admin", date(day, hour)
    )

    @Test fun emptyHistoryContainsOnlyZeroBalances() {
        val points = balancePoints(emptyList(), true, date(26))
        assertEquals(26, points.size)
        assertEquals(setOf(0L), points.map { it.value }.toSet())
    }

    @Test fun weekIncludesOpeningBalanceAndDeductsExpenses() {
        val points = balancePoints(listOf(tx(1, 1000), tx(21, 200), tx(23, 400, false)), false, date(26))
        assertEquals(6, points.size) // Monday 21 through Saturday 26.
        assertEquals(listOf(1200L, 1200L, 800L, 800L, 800L, 800L), points.map { it.value })
    }

    @Test fun futureEntriesAreExcludedAndMidnightBelongsToNewDay() {
        val points = balancePoints(listOf(tx(21, 200, hour = 0), tx(26, 50, hour = 18), tx(27, 500)), false, date(26))
        assertEquals(setOf(200L), points.map { it.value }.toSet())
    }

    @Test fun firstDayOfMonthIsOneRealPointAndCanBeNegative() {
        val points = balancePoints(listOf(tx(1, 250, false)), true, date(1))
        assertEquals(1, points.size)
        assertEquals(-250L, points.single().value)
    }
}
