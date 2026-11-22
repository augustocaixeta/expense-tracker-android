package br.edu.iftm.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.edu.iftm.expensetracker.data.Category

/**
 * Ícone da categoria sobre um quadrado arredondado com a cor da categoria em transparência.
 *
 * @param category categoria exibida.
 * @param size lado do quadrado.
 * @param iconSize tamanho do ícone.
 */
@Composable
fun CategoryBadge(category: Category, size: Dp = 44.dp, iconSize: Dp = 24.dp) {
    val color = category.color()
    Box(
        modifier = Modifier
            .size(size)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = stringResource(category.labelRes()),
            modifier = Modifier.size(iconSize),
            tint = color
        )
    }
}
