package com.example.kitchelper.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchelper.ui.theme.InterFamily

// Colores del componente
private val VERDE_KITCHEN = Color(0xFF4A6B4A)
private val NARANJA_ACTIVO = Color(0xFFFF6B4A)
private val GRIS_INACTIVO = Color.Gray

/**
 * Componente reutilizable de la barra de navegación inferior flotante.
 * Se utiliza en todas las pantallas principales de la aplicación.
 */
@Composable
fun BarraNavegacionInferior(
    modifier: Modifier = Modifier,
    rutaActual: String = "",
    onInicioClick: () -> Unit,
    onRecetasClick: () -> Unit,
    onGuardadosClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onFlechaClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Home,
                    label = "INICIO",
                    color = if (rutaActual == "home") NARANJA_ACTIVO else GRIS_INACTIVO,
                    onClick = onInicioClick
                )
                NavItem(
                    icon = Icons.Default.Restaurant,
                    label = "RECETAS",
                    color = if (rutaActual == "recipes") NARANJA_ACTIVO else GRIS_INACTIVO,
                    onClick = onRecetasClick
                )
                Spacer(modifier = Modifier.width(50.dp))
                NavItem(
                    icon = Icons.Default.Bookmark,
                    label = "GUARDADOS",
                    color = if (rutaActual == "favoritos") NARANJA_ACTIVO else GRIS_INACTIVO,
                    onClick = onGuardadosClick
                )
                NavItem(
                    icon = Icons.Default.Person,
                    label = "PERFIL",
                    color = if (rutaActual == "profile") NARANJA_ACTIVO else GRIS_INACTIVO,
                    onClick = onPerfilClick
                )
            }
        }

        // Botón central flotante
        Surface(
            shape = CircleShape,
            color = VERDE_KITCHEN,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(70.dp)
                .offset(y = (-20).dp)
                .clickable { onFlechaClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Cargar o actualizar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
