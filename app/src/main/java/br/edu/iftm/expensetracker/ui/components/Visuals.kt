package br.edu.iftm.expensetracker.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import br.edu.iftm.expensetracker.R
import br.edu.iftm.expensetracker.data.AmountError
import br.edu.iftm.expensetracker.data.BudgetLevel
import br.edu.iftm.expensetracker.data.Category
import br.edu.iftm.expensetracker.data.PaymentMethod
import br.edu.iftm.expensetracker.ui.theme.DangerColor
import br.edu.iftm.expensetracker.ui.theme.FuelColor
import br.edu.iftm.expensetracker.ui.theme.OtherColor
import br.edu.iftm.expensetracker.ui.theme.PharmacyColor
import br.edu.iftm.expensetracker.ui.theme.SafeColor
import br.edu.iftm.expensetracker.ui.theme.SupermarketColor
import br.edu.iftm.expensetracker.ui.theme.WarningColor
import br.edu.iftm.expensetracker.ui.theme.WarningTextColor

/**
 * Nome da categoria exibido na interface.
 *
 * @receiver categoria.
 * @return id do texto no strings.xml.
 */
@StringRes
fun Category.labelRes(): Int {
    return when (this) {
        Category.SUPERMARKET -> R.string.category_supermarket
        Category.FUEL -> R.string.category_fuel
        Category.PHARMACY -> R.string.category_pharmacy
        Category.OTHER -> R.string.category_other
    }
}

/**
 * Ícone do Material Icons que representa a categoria.
 *
 * @receiver categoria.
 * @return ícone.
 */
fun Category.icon(): ImageVector {
    return when (this) {
        Category.SUPERMARKET -> Icons.Rounded.ShoppingCart
        Category.FUEL -> Icons.Rounded.LocalGasStation
        Category.PHARMACY -> Icons.Rounded.LocalPharmacy
        Category.OTHER -> Icons.Rounded.Category
    }
}

/**
 * Cor de destaque da categoria, usada no ícone.
 *
 * @receiver categoria.
 * @return cor.
 */
fun Category.color(): Color {
    return when (this) {
        Category.SUPERMARKET -> SupermarketColor
        Category.FUEL -> FuelColor
        Category.PHARMACY -> PharmacyColor
        Category.OTHER -> OtherColor
    }
}

/**
 * Nome da forma de pagamento exibido na interface.
 *
 * @receiver forma de pagamento.
 * @return id do texto no strings.xml.
 */
@StringRes
fun PaymentMethod.labelRes(): Int {
    return when (this) {
        PaymentMethod.PIX -> R.string.payment_pix
        PaymentMethod.CASH -> R.string.payment_cash
        PaymentMethod.CREDIT_CARD -> R.string.payment_credit
        PaymentMethod.DEBIT_CARD -> R.string.payment_debit
    }
}

/**
 * Ícone da forma de pagamento.
 *
 * @receiver forma de pagamento.
 * @return ícone.
 */
fun PaymentMethod.icon(): ImageVector {
    return when (this) {
        PaymentMethod.PIX -> Icons.Rounded.QrCode2
        PaymentMethod.CASH -> Icons.Rounded.Payments
        PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_CARD -> Icons.Rounded.CreditCard
    }
}

/**
 * Cor da barra de progresso em cada faixa de alerta.
 *
 * @receiver faixa de alerta.
 * @return verde, amarelo ou vermelho.
 */
fun BudgetLevel.barColor(): Color {
    return when (this) {
        BudgetLevel.NO_LIMIT, BudgetLevel.SAFE -> SafeColor
        BudgetLevel.WARNING -> WarningColor
        BudgetLevel.EXCEEDED -> DangerColor
    }
}

/**
 * Cor do texto de status em cada faixa de alerta. O amarelo fica mais escuro no tema claro
 * para manter o contraste com o fundo branco.
 *
 * @receiver faixa de alerta.
 * @return cor do texto.
 */
@Composable
fun BudgetLevel.textColor(): Color {
    return when (this) {
        BudgetLevel.NO_LIMIT, BudgetLevel.SAFE -> MaterialTheme.colorScheme.onSurfaceVariant
        BudgetLevel.WARNING -> if (isSystemInDarkTheme()) WarningColor else WarningTextColor
        BudgetLevel.EXCEEDED -> DangerColor
    }
}

/**
 * Mensagem exibida para cada erro de valor.
 *
 * @receiver erro de validação.
 * @return id do texto no strings.xml.
 */
@StringRes
fun AmountError.messageRes(): Int {
    return when (this) {
        AmountError.REQUIRED -> R.string.amount_error_required
        AmountError.INVALID -> R.string.amount_error_invalid
        AmountError.NEGATIVE -> R.string.amount_error_negative
        AmountError.NOT_POSITIVE -> R.string.amount_error_not_positive
        AmountError.TOO_LARGE -> R.string.amount_error_too_large
    }
}
