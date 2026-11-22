package br.edu.iftm.expensetracker.data

enum class AmountError {
    REQUIRED,
    INVALID,
    NEGATIVE,
    NOT_POSITIVE,
    TOO_LARGE
}

sealed interface AmountResult {
    data class Valid(val cents: Long) : AmountResult
    data class Invalid(val error: AmountError) : AmountResult
}

const val MAX_AMOUNT_CENTS = 100_000_000L

private const val MAX_INPUT_LENGTH = 12

private val AMOUNT_PATTERN = Regex("""^\d{1,9}([.,]\d{1,2})?$""")

/**
 * Remove do texto digitado tudo o que não for dígito ou separador decimal.
 * Letras, espaços e o sinal de menos não chegam ao campo.
 *
 * @param text texto recebido do teclado ou colado.
 * @return texto apenas com dígitos, vírgula e ponto.
 */
fun sanitizeAmountInput(text: String): String =
    text.filter { it.isDigit() || it == ',' || it == '.' }.take(MAX_INPUT_LENGTH)

/**
 * Converte o valor digitado em centavos. Aceita vírgula ou ponto como separador decimal
 * e no máximo duas casas decimais, como "49,90", "49.9" ou "1200".
 *
 * @param text valor digitado.
 * @param allowZero true quando zero é aceito, como no teto de uma categoria sem limite.
 * @return valor em centavos, ou o motivo da rejeição.
 */
fun parseAmount(text: String, allowZero: Boolean = false): AmountResult {
    val clean = text.trim()
    if (clean.isEmpty()) {
        return AmountResult.Invalid(AmountError.REQUIRED)
    }
    if (clean.startsWith("-")) {
        return AmountResult.Invalid(AmountError.NEGATIVE)
    }
    if (!AMOUNT_PATTERN.matches(clean)) {
        return AmountResult.Invalid(AmountError.INVALID)
    }
    val parts = clean.split(',', '.')
    val whole = parts[0].toLong()
    val fraction = parts.getOrNull(1)?.padEnd(2, '0')?.toLong() ?: 0L
    val cents = whole * 100 + fraction
    return when {
        cents == 0L && !allowZero -> AmountResult.Invalid(AmountError.NOT_POSITIVE)
        cents > MAX_AMOUNT_CENTS -> AmountResult.Invalid(AmountError.TOO_LARGE)
        else -> AmountResult.Valid(cents)
    }
}

/**
 * Converte centavos no texto usado para preencher um campo de valor.
 *
 * @param cents valor em centavos.
 * @return texto como "1200,00", ou vazio quando o valor é zero.
 */
fun centsToInput(cents: Long): String {
    if (cents <= 0) {
        return ""
    }
    return "%d,%02d".format(cents / 100, cents % 100)
}
