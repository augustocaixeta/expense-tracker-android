package br.edu.iftm.expensetracker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    /**
     * Observa os tetos de um usuário.
     *
     * @param userId uid do usuário no Firebase.
     * @return fluxo que emite a lista a cada alteração na tabela.
     */
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun observeAll(userId: String): Flow<List<Budget>>

    /**
     * Insere os tetos novos e atualiza os que já existem.
     *
     * @param budgets tetos de cada categoria.
     */
    @Upsert
    suspend fun upsertAll(budgets: List<Budget>)
}
