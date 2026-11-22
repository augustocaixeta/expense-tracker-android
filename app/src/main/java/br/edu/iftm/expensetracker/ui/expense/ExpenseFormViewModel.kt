package br.edu.iftm.expensetracker.ui.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.edu.iftm.expensetracker.ExpenseTrackerApp
import br.edu.iftm.expensetracker.data.AmountError
import br.edu.iftm.expensetracker.data.AmountResult
import br.edu.iftm.expensetracker.data.AuthRepository
import br.edu.iftm.expensetracker.data.Category
import br.edu.iftm.expensetracker.data.ExpenseRepository
import br.edu.iftm.expensetracker.data.PaymentMethod
import br.edu.iftm.expensetracker.data.parseAmount
import br.edu.iftm.expensetracker.data.sanitizeAmountInput
import br.edu.iftm.expensetracker.ui.navigation.ExpenseForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseFormUiState(
    val amount: String = "",
    val category: Category? = null,
    val description: String = "",
    val paymentMethod: PaymentMethod? = null,
    val amountError: AmountError? = null,
    val categoryMissing: Boolean = false,
    val descriptionMissing: Boolean = false,
    val paymentMissing: Boolean = false,
    val isSaving: Boolean = false,
    val finished: Boolean = false
)

class ExpenseFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val expenses: ExpenseRepository,
    auth: AuthRepository
) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()
    private val initialCategory = savedStateHandle.toRoute<ExpenseForm>().category?.let { name ->
        Category.entries.find { it.name == name }
    }

    private val _state = MutableStateFlow(ExpenseFormUiState(category = initialCategory))
    val state: StateFlow<ExpenseFormUiState> = _state.asStateFlow()

    /**
     * Atualiza o valor digitado. Caracteres que não são dígitos nem separadores são descartados.
     *
     * @param value texto do campo.
     */
    fun onAmountChange(value: String) {
        _state.update { it.copy(amount = sanitizeAmountInput(value), amountError = null) }
    }

    /**
     * Troca a categoria escolhida.
     *
     * @param value categoria tocada.
     */
    fun onCategoryChange(value: Category) {
        _state.update { it.copy(category = value, categoryMissing = false) }
    }

    /**
     * Atualiza a descrição ou o estabelecimento.
     *
     * @param value texto do campo.
     */
    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value.take(MAX_DESCRIPTION_LENGTH), descriptionMissing = false) }
    }

    /**
     * Troca a forma de pagamento escolhida.
     *
     * @param value forma de pagamento tocada.
     */
    fun onPaymentChange(value: PaymentMethod) {
        _state.update { it.copy(paymentMethod = value, paymentMissing = false) }
    }

    /**
     * Valida todos os campos de uma vez e grava a compra. Com algum erro, marca os campos
     * inválidos e não grava nada.
     */
    fun save() {
        val current = _state.value
        if (current.isSaving) {
            return
        }
        val amount = parseAmount(current.amount)
        val category = current.category
        val payment = current.paymentMethod
        val description = current.description.trim()
        if (amount !is AmountResult.Valid || category == null || payment == null || description.isEmpty()) {
            _state.update {
                it.copy(
                    amountError = (amount as? AmountResult.Invalid)?.error,
                    categoryMissing = category == null,
                    descriptionMissing = description.isEmpty(),
                    paymentMissing = payment == null
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            expenses.add(userId, amount.cents, category, description, payment)
            _state.update { it.copy(isSaving = false, finished = true) }
        }
    }

    companion object {
        private const val MAX_DESCRIPTION_LENGTH = 60

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenseTrackerApp
                ExpenseFormViewModel(createSavedStateHandle(), app.expenses, app.auth)
            }
        }
    }
}
