package br.edu.iftm.expensetracker.data

enum class BudgetLevel {
    NO_LIMIT,
    SAFE,
    WARNING,
    EXCEEDED
}

const val WARNING_THRESHOLD = 0.8f

/**
 * Consumo de uma categoria no mês em relação ao teto dela.
 */
data class CategorySummary(
    val category: Category,
    val spentCents: Long,
    val limitCents: Long
) {
    val progress: Float
        get() = progressOf(spentCents, limitCents)

    val remainingCents: Long
        get() = limitCents - spentCents

    val level: BudgetLevel
        get() = levelOf(spentCents, limitCents)
}

/**
 * Resumo do mês: o consumo de cada categoria e os totais somados.
 */
data class MonthSummary(val categories: List<CategorySummary> = emptyList()) {
    val spentCents: Long
        get() = categories.sumOf { it.spentCents }

    val limitCents: Long
        get() = categories.sumOf { it.limitCents }

    val remainingCents: Long
        get() = limitCents - spentCents

    val progress: Float
        get() = progressOf(spentCents, limitCents)

    val level: BudgetLevel
        get() = levelOf(spentCents, limitCents)
}

/**
 * Monta o resumo do mês com uma linha para cada categoria, mesmo as que ainda não têm compras.
 *
 * @param expenses compras do mês.
 * @param budgets tetos gravados pelo usuário.
 * @return resumo com as categorias na ordem do enum.
 */
fun summarize(expenses: List<Expense>, budgets: List<Budget>): MonthSummary {
    val spentByCategory = expenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amountCents } }
    val limitByCategory = budgets.associate { it.category to it.limitCents }
    val categories = Category.entries.map { category ->
        CategorySummary(
            category = category,
            spentCents = spentByCategory[category] ?: 0L,
            limitCents = limitByCategory[category] ?: 0L
        )
    }
    return MonthSummary(categories)
}

/**
 * Fração do teto já consumida. Pode passar de 1 quando o teto é estourado.
 *
 * @param spentCents total gasto.
 * @param limitCents teto definido.
 * @return fração consumida, ou zero quando não há teto.
 */
fun progressOf(spentCents: Long, limitCents: Long): Float {
    if (limitCents <= 0) {
        return 0f
    }
    return spentCents.toFloat() / limitCents
}

/**
 * Faixa de alerta do consumo: verde até 80% do teto, amarelo a partir de 80% e vermelho acima do teto.
 *
 * @param spentCents total gasto.
 * @param limitCents teto definido.
 * @return faixa de alerta.
 */
fun levelOf(spentCents: Long, limitCents: Long): BudgetLevel {
    return when {
        limitCents <= 0 -> BudgetLevel.NO_LIMIT
        spentCents > limitCents -> BudgetLevel.EXCEEDED
        progressOf(spentCents, limitCents) >= WARNING_THRESHOLD -> BudgetLevel.WARNING
        else -> BudgetLevel.SAFE
    }
}
