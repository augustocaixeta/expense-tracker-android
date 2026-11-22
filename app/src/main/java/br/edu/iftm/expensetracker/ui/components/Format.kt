package br.edu.iftm.expensetracker.ui.components

import br.edu.iftm.expensetracker.ui.theme.PtBr
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(PtBr)

/**
 * Formata um valor em reais.
 *
 * @param cents valor em centavos.
 * @return texto como "R$ 1.200,00".
 */
fun formatCurrency(cents: Long): String = currencyFormat.format(BigDecimal.valueOf(cents, 2))

/**
 * Nome do mês com o ano, com a primeira letra maiúscula.
 *
 * @param month mês exibido.
 * @param pattern padrão lido do strings.xml.
 * @return texto como "Novembro de 2026".
 */
fun formatMonth(month: YearMonth, pattern: String): String {
    return month
        .format(DateTimeFormatter.ofPattern(pattern, PtBr))
        .replaceFirstChar { it.uppercase() }
}

/**
 * Dia e mês de uma compra.
 *
 * @param date dia da compra.
 * @param pattern padrão lido do strings.xml.
 * @return texto como "22/11".
 */
fun formatDay(date: LocalDate, pattern: String): String = date.format(DateTimeFormatter.ofPattern(pattern, PtBr))
