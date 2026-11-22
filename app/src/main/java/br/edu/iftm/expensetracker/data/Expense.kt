package br.edu.iftm.expensetracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Compra lançada pelo usuário. O valor fica em centavos para evitar erros de arredondamento.
 */
@Entity(tableName = "expenses", indices = [Index(value = ["userId", "spentOn"])])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val amountCents: Long,
    val category: Category,
    val description: String,
    val paymentMethod: PaymentMethod,
    val spentOn: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
)
