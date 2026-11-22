package br.edu.iftm.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.iftm.expensetracker.ui.navigation.AppNavHost
import br.edu.iftm.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    /**
     * Desenha o app de ponta a ponta e abre o grafo de navegação.
     *
     * @param savedInstanceState estado salvo pelo sistema, se houver.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                AppNavHost()
            }
        }
    }
}
