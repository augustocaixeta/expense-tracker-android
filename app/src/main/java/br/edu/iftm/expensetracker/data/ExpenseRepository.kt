package br.edu.iftm.expensetracker.data

import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao, private val budgetDao: BudgetDao) {

    /**
     * Observa as compras de um mês.
     *
     * @param userId uid do usuário no Firebase.
     * @param month mês consultado.
     * @return fluxo que emite a lista a cada compra lançada ou excluída.
     */
    fun observeMonth(userId: String, month: YearMonth): Flow<List<Expense>> =
        expenseDao.observeBetween(userId, month.atDay(1), month.atEndOfMonth())

    /**
     * Observa os tetos de cada categoria.
     *
     * @param userId uid do usuário no Firebase.
     * @return fluxo que emite a lista a cada alteração.
     */
    fun observeBudgets(userId: String): Flow<List<Budget>> = budgetDao.observeAll(userId)

    /**
     * Lança uma compra.
     *
     * @param userId uid do usuário no Firebase.
     * @param amountCents valor em centavos, já validado.
     * @param category categoria escolhida.
     * @param description descrição ou estabelecimento.
     * @param paymentMethod forma de pagamento.
     * @param spentOn dia da compra, hoje por padrão.
     */
    suspend fun add(
        userId: String,
        amountCents: Long,
        category: Category,
        description: String,
        paymentMethod: PaymentMethod,
        spentOn: LocalDate = LocalDate.now()
    ) {
        require(amountCents > 0) { "O valor da compra deve ser positivo" }
        expenseDao.insert(
            Expense(
                userId = userId,
                amountCents = amountCents,
                category = category,
                description = description.trim(),
                paymentMethod = paymentMethod,
                spentOn = spentOn
            )
        )
    }

    /**
     * Exclui uma compra.
     *
     * @param userId uid do usuário no Firebase.
     * @param expenseId identificador da compra.
     */
    suspend fun delete(userId: String, expenseId: Long) {
        expenseDao.delete(userId, expenseId)
    }

    /**
     * Grava o teto de cada categoria. Categorias ausentes no mapa ficam sem teto.
     *
     * @param userId uid do usuário no Firebase.
     * @param limits teto em centavos por categoria.
     */
    suspend fun saveBudgets(userId: String, limits: Map<Category, Long>) {
        val budgets = Category.entries.map { category ->
            Budget(userId = userId, category = category, limitCents = (limits[category] ?: 0L).coerceAtLeast(0L))
        }
        budgetDao.upsertAll(budgets)
    }
}
