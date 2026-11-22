package br.edu.iftm.expensetracker.ui.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.expensetracker.R
import br.edu.iftm.expensetracker.data.Category
import br.edu.iftm.expensetracker.data.PaymentMethod
import br.edu.iftm.expensetracker.ui.components.CategoryBadge
import br.edu.iftm.expensetracker.ui.components.icon
import br.edu.iftm.expensetracker.ui.components.labelRes
import br.edu.iftm.expensetracker.ui.components.messageRes

/**
 * Lançamento rápido de uma compra: valor, categoria, descrição e forma de pagamento.
 *
 * @param onBack volta para o painel.
 * @param viewModel estado do formulário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    onBack: () -> Unit,
    viewModel: ExpenseFormViewModel = viewModel(factory = ExpenseFormViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val amountError = state.amountError

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.form_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.form_field_amount)) },
                placeholder = { Text(text = stringResource(R.string.amount_hint)) },
                prefix = { Text(text = stringResource(R.string.currency_prefix)) },
                supportingText = if (amountError != null) {
                    { Text(text = stringResource(amountError.messageRes())) }
                } else {
                    null
                },
                isError = amountError != null,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            SectionTitle(
                text = stringResource(R.string.form_section_category),
                error = if (state.categoryMissing) stringResource(R.string.form_error_category) else null
            )
            CategoryPicker(selected = state.category, onSelect = viewModel::onCategoryChange)
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.form_field_description)) },
                placeholder = { Text(text = stringResource(R.string.form_field_description_hint)) },
                supportingText = if (state.descriptionMissing) {
                    { Text(text = stringResource(R.string.form_error_description)) }
                } else {
                    null
                },
                isError = state.descriptionMissing,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                )
            )
            SectionTitle(
                text = stringResource(R.string.form_section_payment),
                error = if (state.paymentMissing) stringResource(R.string.form_error_payment) else null
            )
            PaymentPicker(selected = state.paymentMethod, onSelect = viewModel::onPaymentChange)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(R.string.form_action_save), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Título de uma seção do formulário, com a mensagem de erro ao lado quando a escolha está faltando.
 *
 * @param text título da seção.
 * @param error mensagem de erro, ou null.
 */
@Composable
private fun SectionTitle(text: String, error: String?) {
    Column {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * Grade 2 por 2 com as categorias. A escolhida ganha borda e fundo na cor primária.
 *
 * @param selected categoria escolhida, ou null.
 * @param onSelect recebe a categoria tocada.
 */
@Composable
private fun CategoryPicker(selected: Category?, onSelect: (Category) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Category.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    CategoryOption(
                        category = category,
                        selected = category == selected,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Opção de categoria com ícone e nome.
 *
 * @param category categoria da opção.
 * @param selected true quando é a categoria escolhida.
 * @param onClick escolhe a categoria.
 * @param modifier modificador aplicado ao cartão.
 */
@Composable
private fun CategoryOption(category: Category, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryBadge(category = category, size = 36.dp, iconSize = 20.dp)
            Spacer(Modifier.size(10.dp))
            Text(text = stringResource(category.labelRes()), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Chips com as formas de pagamento, em duas linhas.
 *
 * @param selected forma de pagamento escolhida, ou null.
 * @param onSelect recebe a forma tocada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentPicker(selected: PaymentMethod?, onSelect: (PaymentMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PaymentMethod.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { method ->
                    FilterChip(
                        selected = method == selected,
                        onClick = { onSelect(method) },
                        label = { Text(text = stringResource(method.labelRes())) },
                        leadingIcon = {
                            Icon(imageVector = method.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
