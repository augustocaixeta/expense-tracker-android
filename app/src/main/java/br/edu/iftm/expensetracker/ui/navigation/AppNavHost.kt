package br.edu.iftm.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.iftm.expensetracker.ui.auth.AuthViewModel
import br.edu.iftm.expensetracker.ui.auth.LoginScreen
import br.edu.iftm.expensetracker.ui.auth.RegisterScreen
import br.edu.iftm.expensetracker.ui.budget.BudgetScreen
import br.edu.iftm.expensetracker.ui.dashboard.DashboardScreen
import br.edu.iftm.expensetracker.ui.expense.ExpenseFormScreen

/**
 * Grafo de navegação: Login e Registro levam ao painel depois da autenticação,
 * e o painel abre o lançamento de compras e a definição dos tetos.
 * Com uma sessão ativa no Firebase, o app já abre no painel.
 *
 * @param navController controlador que guarda a pilha de telas.
 * @param authViewModel estado da autenticação, compartilhado entre Login e Registro.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val start: Any = remember { if (authViewModel.state.value.isLoggedIn) Dashboard else Login }

    LaunchedEffect(authState.isLoggedIn) {
        val destination = navController.currentDestination
        val onAuthScreen = destination?.hasRoute<Login>() == true || destination?.hasRoute<Register>() == true
        if (authState.isLoggedIn && onAuthScreen) {
            navController.navigate(Dashboard) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable<Login> { entry ->
            LoginScreen(
                viewModel = authViewModel,
                onGoToRegister = { navController.navigateFrom(entry, Register) }
            )
        }
        composable<Register> { entry ->
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popFrom(entry) }
            )
        }
        composable<Dashboard> { entry ->
            DashboardScreen(
                onAddExpense = { category -> navController.navigateFrom(entry, ExpenseForm(category?.name)) },
                onEditBudgets = { navController.navigateFrom(entry, Budgets) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Login) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<ExpenseForm> { entry ->
            ExpenseFormScreen(onBack = { navController.popFrom(entry) })
        }
        composable<Budgets> { entry ->
            BudgetScreen(onBack = { navController.popFrom(entry) })
        }
    }
}

/**
 * Abre uma rota só se a tela de origem ainda estiver ativa, evitando telas duplicadas com toques repetidos.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu a navegação.
 * @param route destino.
 */
private fun <T : Any> NavHostController.navigateFrom(entry: NavBackStackEntry, route: T) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

/**
 * Volta uma tela só se a tela de origem ainda estiver ativa, para não fechar o painel por engano.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu para voltar.
 */
private fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}
