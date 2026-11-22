package br.edu.iftm.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.expensetracker.ExpenseTrackerApp
import br.edu.iftm.expensetracker.data.AuthRepository
import br.edu.iftm.expensetracker.data.Expense
import br.edu.iftm.expensetracker.data.ExpenseRepository
import br.edu.iftm.expensetracker.data.MonthSummary
import br.edu.iftm.expensetracker.data.summarize
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "",
    val month: YearMonth = YearMonth.now(),
    val summary: MonthSummary = MonthSummary(),
    val expenses: List<Expense> = emptyList(),
    val loaded: Boolean = false
) {
    val hasBudgets: Boolean
        get() = summary.limitCents > 0
}

class DashboardViewModel(private val expenses: ExpenseRepository, auth: AuthRepository) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()
    private val userName = auth.currentUser?.let { user ->
        user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@")
    }.orEmpty()
    private val month = YearMonth.now()

    val state: StateFlow<DashboardUiState> = combine(
        expenses.observeMonth(userId, month),
        expenses.observeBudgets(userId)
    ) { monthExpenses, budgets ->
        DashboardUiState(
            userName = userName,
            month = month,
            summary = summarize(monthExpenses, budgets),
            expenses = monthExpenses,
            loaded = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(userName = userName, month = month))

    /**
     * Exclui uma compra. O painel se atualiza pelo fluxo do Room.
     *
     * @param expenseId identificador da compra.
     */
    fun delete(expenseId: Long) {
        viewModelScope.launch {
            expenses.delete(userId, expenseId)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenseTrackerApp
                DashboardViewModel(app.expenses, app.auth)
            }
        }
    }
}
