package br.edu.iftm.expensetracker.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetSummaryTest {

    @Test
    fun levelFollowsGreenYellowRedThresholds() {
        assertEquals(BudgetLevel.NO_LIMIT, levelOf(5000, 0))
        assertEquals(BudgetLevel.SAFE, levelOf(0, 10000))
        assertEquals(BudgetLevel.SAFE, levelOf(7999, 10000))
        assertEquals(BudgetLevel.WARNING, levelOf(8000, 10000))
        assertEquals(BudgetLevel.WARNING, levelOf(10000, 10000))
        assertEquals(BudgetLevel.EXCEEDED, levelOf(10001, 10000))
    }

    @Test
    fun summarizeGroupsByCategoryAndComputesTotals() {
        val today = LocalDate.now()
        val expenses = listOf(
            expense(30000, Category.SUPERMARKET, today),
            expense(20000, Category.SUPERMARKET, today),
            expense(55000, Category.FUEL, today),
            expense(4000, Category.OTHER, today)
        )
        val budgets = listOf(
            Budget("u1", Category.SUPERMARKET, 120000),
            Budget("u1", Category.FUEL, 50000),
            Budget("u1", Category.PHARMACY, 25000)
        )

        val summary = summarize(expenses, budgets)

        assertEquals(Category.entries.size, summary.categories.size)
        assertEquals(109000L, summary.spentCents)
        assertEquals(195000L, summary.limitCents)
        assertEquals(86000L, summary.remainingCents)

        val fuel = summary.categories.first { it.category == Category.FUEL }
        assertEquals(BudgetLevel.EXCEEDED, fuel.level)
        assertEquals(-5000L, fuel.remainingCents)

        val pharmacy = summary.categories.first { it.category == Category.PHARMACY }
        assertEquals(0L, pharmacy.spentCents)
        assertEquals(BudgetLevel.SAFE, pharmacy.level)

        val other = summary.categories.first { it.category == Category.OTHER }
        assertEquals(BudgetLevel.NO_LIMIT, other.level)
    }

    private fun expense(cents: Long, category: Category, date: LocalDate) = Expense(
        userId = "u1",
        amountCents = cents,
        category = category,
        description = "Teste",
        paymentMethod = PaymentMethod.PIX,
        spentOn = date
    )
}
