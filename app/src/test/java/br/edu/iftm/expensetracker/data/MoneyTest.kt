package br.edu.iftm.expensetracker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun acceptsCommaAndDotAsDecimalSeparator() {
        assertEquals(AmountResult.Valid(4990), parseAmount("49,90"))
        assertEquals(AmountResult.Valid(4990), parseAmount("49.90"))
        assertEquals(AmountResult.Valid(4950), parseAmount("49,5"))
        assertEquals(AmountResult.Valid(120000), parseAmount("1200"))
    }

    @Test
    fun rejectsEmptyText() {
        assertEquals(AmountResult.Invalid(AmountError.REQUIRED), parseAmount(""))
        assertEquals(AmountResult.Invalid(AmountError.REQUIRED), parseAmount("   "))
    }

    @Test
    fun rejectsNegativeValues() {
        assertEquals(AmountResult.Invalid(AmountError.NEGATIVE), parseAmount("-10"))
        assertEquals(AmountResult.Invalid(AmountError.NEGATIVE), parseAmount("-0,50"))
    }

    @Test
    fun rejectsNonNumericText() {
        assertEquals(AmountResult.Invalid(AmountError.INVALID), parseAmount("abc"))
        assertEquals(AmountResult.Invalid(AmountError.INVALID), parseAmount("12a"))
        assertEquals(AmountResult.Invalid(AmountError.INVALID), parseAmount("1,2,3"))
        assertEquals(AmountResult.Invalid(AmountError.INVALID), parseAmount("10,999"))
        assertEquals(AmountResult.Invalid(AmountError.INVALID), parseAmount(",50"))
    }

    @Test
    fun rejectsZeroUnlessAllowed() {
        assertEquals(AmountResult.Invalid(AmountError.NOT_POSITIVE), parseAmount("0"))
        assertEquals(AmountResult.Invalid(AmountError.NOT_POSITIVE), parseAmount("0,00"))
        assertEquals(AmountResult.Valid(0), parseAmount("0", allowZero = true))
    }

    @Test
    fun rejectsValuesAboveTheMaximum() {
        assertEquals(AmountResult.Valid(MAX_AMOUNT_CENTS), parseAmount("1000000"))
        assertEquals(AmountResult.Invalid(AmountError.TOO_LARGE), parseAmount("1000000,01"))
    }

    @Test
    fun sanitizeKeepsOnlyDigitsAndSeparators() {
        assertEquals("1250,90", sanitizeAmountInput("R$ 1250,90"))
        assertEquals("50", sanitizeAmountInput("-50"))
        assertEquals("", sanitizeAmountInput("abc"))
    }

    @Test
    fun centsToInputFormatsWithComma() {
        assertEquals("1200,00", centsToInput(120000))
        assertEquals("0,05", centsToInput(5))
        assertEquals("", centsToInput(0))
    }
}
