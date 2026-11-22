package br.edu.iftm.expensetracker.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.expensetracker.R
import br.edu.iftm.expensetracker.data.BudgetLevel
import br.edu.iftm.expensetracker.data.Category
import br.edu.iftm.expensetracker.data.CategorySummary
import br.edu.iftm.expensetracker.data.Expense
import br.edu.iftm.expensetracker.data.MonthSummary
import br.edu.iftm.expensetracker.ui.components.CategoryBadge
import br.edu.iftm.expensetracker.ui.components.barColor
import br.edu.iftm.expensetracker.ui.components.formatCurrency
import br.edu.iftm.expensetracker.ui.components.formatDay
import br.edu.iftm.expensetracker.ui.components.formatMonth
import br.edu.iftm.expensetracker.ui.components.labelRes
import br.edu.iftm.expensetracker.ui.components.textColor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Painel do mês corrente: resumo global, consumo de cada categoria com alerta de cores
 * e a lista de compras lançadas.
 *
 * @param onAddExpense abre o lançamento de uma compra, com a categoria tocada já escolhida, se houver.
 * @param onEditBudgets abre a definição dos tetos.
 * @param onLogout encerra a sessão e volta ao login.
 * @param viewModel estado do painel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpense: (Category?) -> Unit,
    onEditBudgets: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_greeting, state.userName),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatMonth(state.month, stringResource(R.string.month_pattern)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditBudgets) {
                        Icon(
                            imageVector = Icons.Outlined.Savings,
                            contentDescription = stringResource(R.string.cd_budgets)
                        )
                    }
                    IconButton(onClick = { confirmLogout = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = stringResource(R.string.cd_logout)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.dashboard_add_expense)) },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                onClick = { onAddExpense(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (!state.loaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SummaryCard(summary = state.summary)
            }
            if (!state.hasBudgets) {
                item {
                    NoBudgetsCard(onEditBudgets = onEditBudgets)
                }
            }
            item {
                SectionTitle(text = stringResource(R.string.dashboard_section_categories))
            }
            items(state.summary.categories, key = { it.category.name }) { summary ->
                CategoryCard(summary = summary, onClick = { onAddExpense(summary.category) })
            }
            item {
                SectionTitle(text = stringResource(R.string.dashboard_section_expenses))
            }
            if (state.expenses.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.dashboard_expenses_empty),
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.expenses, key = { it.id }) { expense ->
                ExpenseItem(expense = expense, onDelete = { pendingDelete = expense })
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    }
                ) {
                    Text(text = stringResource(R.string.logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.logout_title)) },
            text = { Text(text = stringResource(R.string.logout_message)) }
        )
    }

    pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        viewModel.delete(expense.id)
                    }
                ) {
                    Text(text = stringResource(R.string.delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.delete_message,
                        expense.description,
                        formatCurrency(expense.amountCents)
                    )
                )
            }
        )
    }
}

/**
 * Cartão principal com o total gasto no mês, o teto somado das categorias e o saldo disponível.
 *
 * @param summary resumo do mês.
 */
@Composable
private fun SummaryCard(summary: MonthSummary) {
    val animated by animateFloatAsState(targetValue = summary.progress.coerceIn(0f, 1f), label = "summary")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.summary_spent),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatCurrency(summary.spentCents),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            if (summary.level == BudgetLevel.NO_LIMIT) {
                return@Column
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = summary.level.barColor(),
                trackColor = Color.White.copy(alpha = 0.25f),
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(14.dp))
            Row {
                SummaryValue(
                    label = stringResource(R.string.summary_limit),
                    value = formatCurrency(summary.limitCents),
                    modifier = Modifier.weight(1f)
                )
                SummaryValue(
                    label = stringResource(
                        if (summary.remainingCents >= 0) R.string.summary_available else R.string.summary_over
                    ),
                    value = formatCurrency(abs(summary.remainingCents)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Rótulo pequeno e valor em destaque dentro do cartão de resumo.
 *
 * @param label rótulo acima do valor.
 * @param value valor formatado.
 * @param modifier modificador aplicado à coluna.
 */
@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

/**
 * Convite exibido enquanto nenhuma categoria tem teto definido.
 *
 * @param onEditBudgets abre a definição dos tetos.
 */
@Composable
private fun NoBudgetsCard(onEditBudgets: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.no_budgets_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.no_budgets_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            FilledTonalButton(onClick = onEditBudgets) {
                Text(text = stringResource(R.string.no_budgets_action))
            }
        }
    }
}

/**
 * Cartão de uma categoria com o valor gasto, o teto, o percentual consumido e a barra de progresso
 * colorida pela faixa de alerta. Tocar no cartão abre o lançamento já com a categoria escolhida.
 *
 * @param summary consumo da categoria.
 * @param onClick abre o lançamento de uma compra nessa categoria.
 */
@Composable
private fun CategoryCard(summary: CategorySummary, onClick: () -> Unit) {
    val animated by animateFloatAsState(targetValue = summary.progress.coerceIn(0f, 1f), label = "category")
    val hasLimit = summary.level != BudgetLevel.NO_LIMIT
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(category = summary.category)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(summary.category.labelRes()),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (hasLimit) {
                            stringResource(
                                R.string.category_spent_of_limit,
                                formatCurrency(summary.spentCents),
                                formatCurrency(summary.limitCents)
                            )
                        } else {
                            stringResource(R.string.category_spent, formatCurrency(summary.spentCents))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasLimit) {
                    Text(
                        text = stringResource(R.string.percent, (summary.progress * 100).roundToInt()),
                        style = MaterialTheme.typography.titleMedium,
                        color = summary.level.textColor()
                    )
                }
            }
            if (hasLimit) {
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = summary.level.barColor(),
                    trackColor = summary.level.barColor().copy(alpha = 0.18f),
                    drawStopIndicator = {}
                )
            }
            Text(
                text = statusText(summary),
                style = MaterialTheme.typography.bodyMedium,
                color = summary.level.textColor()
            )
        }
    }
}

/**
 * Frase de status da categoria conforme a faixa de alerta.
 *
 * @param summary consumo da categoria.
 * @return texto com o saldo restante ou com o valor que passou do teto.
 */
@Composable
private fun statusText(summary: CategorySummary): String {
    val remaining = formatCurrency(abs(summary.remainingCents))
    return when (summary.level) {
        BudgetLevel.NO_LIMIT -> stringResource(R.string.status_no_limit)
        BudgetLevel.SAFE -> stringResource(R.string.status_safe, remaining)
        BudgetLevel.WARNING -> stringResource(R.string.status_warning, remaining)
        BudgetLevel.EXCEEDED -> stringResource(R.string.status_exceeded, remaining)
    }
}

/**
 * Linha de uma compra com a categoria, a descrição, a forma de pagamento, o dia e o valor.
 *
 * @param expense compra exibida.
 * @param onDelete pede a exclusão da compra.
 */
@Composable
private fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryBadge(category = expense.category, size = 40.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.expense_meta,
                        stringResource(expense.paymentMethod.labelRes()),
                        formatDay(expense.spentOn, stringResource(R.string.day_pattern))
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = formatCurrency(expense.amountCents), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.cd_delete_expense),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Título de uma seção do painel.
 *
 * @param text texto do título.
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleMedium
    )
}
