package br.edu.iftm.expensetracker

import android.app.Application
import br.edu.iftm.expensetracker.data.AppDatabase
import br.edu.iftm.expensetracker.data.AuthRepository
import br.edu.iftm.expensetracker.data.ExpenseRepository

class ExpenseTrackerApp : Application() {

    val auth by lazy { AuthRepository() }

    private val database by lazy { AppDatabase.build(this) }

    val expenses by lazy { ExpenseRepository(database.expenseDao(), database.budgetDao()) }
}
