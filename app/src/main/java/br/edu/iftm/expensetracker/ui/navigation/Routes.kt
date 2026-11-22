package br.edu.iftm.expensetracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object Dashboard

@Serializable
data object Budgets

@Serializable
data class ExpenseForm(val category: String? = null)
