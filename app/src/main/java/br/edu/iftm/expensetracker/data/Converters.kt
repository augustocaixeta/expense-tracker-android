package br.edu.iftm.expensetracker.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    /**
     * Lê a data gravada como número de dias desde 01/01/1970.
     *
     * @param value dias desde a época.
     * @return data correspondente.
     */
    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    /**
     * Grava a data como número de dias, o que permite filtrar o mês com BETWEEN.
     *
     * @param date data da compra.
     * @return dias desde a época.
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()
}
