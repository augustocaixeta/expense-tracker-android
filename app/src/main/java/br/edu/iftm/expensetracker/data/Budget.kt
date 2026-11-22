package br.edu.iftm.expensetracker.data

import androidx.room.Entity

/**
 * Teto mensal de uma categoria. Um teto igual a zero significa que a categoria não tem limite.
 */
@Entity(tableName = "budgets", primaryKeys = ["userId", "category"])
data class Budget(
    val userId: String,
    val category: Category,
    val limitCents: Long
)
