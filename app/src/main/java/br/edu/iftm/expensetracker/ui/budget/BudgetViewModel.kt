package br.edu.iftm.expensetracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.expensetracker.ExpenseTrackerApp
import br.edu.iftm.expensetracker.data.AmountError
import br.edu.iftm.expensetracker.data.AmountResult
import br.edu.iftm.expensetracker.data.AuthRepository
import br.edu.iftm.expensetracker.data.Category
import br.edu.iftm.expensetracker.data.ExpenseRepository
import br.edu.iftm.expensetracker.data.centsToInput
import br.edu.iftm.expensetracker.data.parseAmount
import br.edu.iftm.expensetracker.data.sanitizeAmountInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetUiState(
    val inputs: Map<Category, String> = Category.entries.associateWith { "" },
    val errors: Map<Category, AmountError> = emptyMap(),
    val loaded: Boolean = false,
    val finished: Boolean = false
) {
    /**
     * Soma dos tetos digitados que já são válidos, para mostrar o teto total enquanto o usuário digita.
     */
    val totalCents: Long
        get() = inputs.values.sumOf { text ->
            (parseLimit(text) as? AmountResult.Valid)?.cents ?: 0L
        }
}

/**
 * Valida o teto de uma categoria. Campo vazio ou zero significa categoria sem teto.
 *
 * @param text valor digitado.
 * @return teto em centavos, ou o motivo da rejeição.
 */
private fun parseLimit(text: String): AmountResult {
    if (text.isBlank()) {
        return AmountResult.Valid(0L)
    }
    return parseAmount(text, allowZero = true)
}

class BudgetViewModel(private val expenses: ExpenseRepository, auth: AuthRepository) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()

    private val _state = MutableStateFlow(BudgetUiState())
    val state: StateFlow<BudgetUiState> = _state.asStateFlow()

    init {
        loadCurrentLimits()
    }

    /**
     * Atualiza o teto digitado de uma categoria, descartando caracteres inválidos.
     *
     * @param category categoria do campo.
     * @param value texto do campo.
     */
    fun onLimitChange(category: Category, value: String) {
        _state.update { current ->
            current.copy(
                inputs = current.inputs + (category to sanitizeAmountInput(value)),
                errors = current.errors - category
            )
        }
    }

    /**
     * Valida todos os campos e grava os tetos. Com algum erro, marca os campos inválidos e não grava nada.
     */
    fun save() {
        val current = _state.value
        val results = current.inputs.mapValues { (_, text) -> parseLimit(text) }
        val errors = results
            .mapNotNull { (category, result) -> (result as? AmountResult.Invalid)?.let { category to it.error } }
            .toMap()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        val limits = results.mapValues { (_, result) -> (result as AmountResult.Valid).cents }
        viewModelScope.launch {
            expenses.saveBudgets(userId, limits)
            _state.update { it.copy(finished = true) }
        }
    }

    /**
     * Preenche os campos uma única vez com os tetos já gravados.
     */
    private fun loadCurrentLimits() {
        viewModelScope.launch {
            val saved = expenses.observeBudgets(userId).first().associate { it.category to it.limitCents }
            _state.update { current ->
                current.copy(
                    inputs = Category.entries.associateWith { centsToInput(saved[it] ?: 0L) },
                    loaded = true
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenseTrackerApp
                BudgetViewModel(app.expenses, app.auth)
            }
        }
    }
}
