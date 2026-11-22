package br.edu.iftm.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    /**
     * Observa as compras de um usuário em um intervalo de datas, da mais recente para a mais antiga.
     *
     * @param userId uid do usuário no Firebase.
     * @param start primeiro dia do intervalo.
     * @param end último dia do intervalo.
     * @return fluxo que emite a lista a cada alteração na tabela.
     */
    @Query(
        "SELECT * FROM expenses WHERE userId = :userId AND spentOn BETWEEN :start AND :end " +
            "ORDER BY spentOn DESC, createdAt DESC"
    )
    fun observeBetween(userId: String, start: LocalDate, end: LocalDate): Flow<List<Expense>>

    /**
     * Grava uma compra nova.
     *
     * @param expense compra lançada.
     */
    @Insert
    suspend fun insert(expense: Expense)

    /**
     * Remove uma compra do usuário.
     *
     * @param userId uid do usuário no Firebase.
     * @param id identificador da compra.
     */
    @Query("DELETE FROM expenses WHERE userId = :userId AND id = :id")
    suspend fun delete(userId: String, id: Long)
}
