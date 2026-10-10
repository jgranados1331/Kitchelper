package com.example.kitchelper.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.model.Receta

// ============ HEADER CON MENÚ DESPLEGABLE ============
@Composable
fun HeaderHome(
    nombreUsuario: String,
    menuExpandido: Boolean,
    onToggleMenu: () -> Unit,
    onPerfilClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nivel - Principiante",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "KITCHENPER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
            }

            // Avatar con menú desplegable
            Box {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4A6B4A),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onToggleMenu() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            nombreUsuario.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                // Menú desplegable
                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = onToggleMenu,
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Perfil") },
                        onClick = {
                            onToggleMenu()
                            onPerfilClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Configuración") },
                        onClick = {
                            onToggleMenu()
                            onConfiguracionClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión", color = Color.Red) },
                        onClick = {
                            onToggleMenu()
                            onCerrarSesionClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

// ============ AVISO COMPLETAR PERFIL ============
@Composable
fun AvisoCompletarPerfil(onCompletarClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color(0xFFC5D86D),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Completa tu perfil",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
                Text(
                    "Queremos estar listos para completar tu información para saber tus gustos y alergias",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            TextButton(onClick = onCompletarClick) {
                Text("Completar", color = Color(0xFFC5D86D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============ RECETA DESTACADA ============
@Composable
fun RecetaDestacada(receta: Receta, onEmpezar: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4A4A4A)
    ) {
        Box(modifier = Modifier.height(220.dp)) {
            // Imagen de fondo (placeholder por ahora)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    "RECETA DESTACADA · ${receta.tiempoMinutos} MIN",
                    fontSize = 10.sp,
                    color = Color(0xFFC5D86D),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    receta.titulo,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    receta.descripcion,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEmpezar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B4A)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("EMPEZAR", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ============ GUÍA INTERACTIVA AR ============
@Composable
fun GuiaInteractivaAR(onCalibrarClick: () -> Unit, onVistaARClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Guía interactiva en AR",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF4A6B4A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "PASO 1 - DETECCIÓN",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Guía para configuración de AR para detectar tu tabla de picar o tu zona de trabajo para poder detectar todos tus cortes",
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = onCalibrarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5D86D)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("● Calibra tu Guía", color = Color(0xFF4A4A4A), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onVistaARClick) {
                        Text("Vista AR →", color = Color.White)
                    }
                }
            }
        }
    }
}

// ============ SELECTOR DE NIVEL ============
@Composable
fun SelectorNivel(nivelSeleccionado: String, onNivelChange: (String) -> Unit) {
    val niveles = listOf("Principiante", "Fácil", "Medio", "Difícil")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(niveles) { nivel ->
            FilterChip(
                selected = nivelSeleccionado == nivel,
                onClick = { onNivelChange(nivel) },
                label = { Text(nivel, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4A6B4A),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ============ TARJETA DE RECETA ============
@Composable
fun TarjetaReceta(receta: Receta, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column {
            // Imagen placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    receta.titulo,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("${receta.tiempoMinutos} min", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(receta.dificultad, fontSize = 11.sp, color = Color(0xFFC5D86D))
                }
            }
        }
    }
}

// ============ TARJETA DE PRÁCTICA ============
@Composable
fun TarjetaPractica(practica: PracticaBasica, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    practica.titulo,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
                Text(
                    practica.descripcion,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F0F0)
                ) {
                    Text(
                        "${practica.tiempoMinutos} min",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ============ BARRA DE NAVEGACIÓN INFERIOR ============
@Composable
fun BarraNavegacionInferior(
    modifier: Modifier = Modifier,
    onInicioClick: () -> Unit,
    onRecetasClick: () -> Unit,
    onGuardadosClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onFlechaClick: () -> Unit
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
                NavItem(Icons.Default.Home, "INICIO", Color(0xFFFF6B4A), onInicioClick)
                NavItem(Icons.Default.Restaurant, "RECETAS", Color.Gray, onRecetasClick)
                Spacer(modifier = Modifier.width(50.dp)) // Espacio para el botón central
                NavItem(Icons.Default.Bookmark, "GUARDADOS", Color.Gray, onGuardadosClick)
                NavItem(Icons.Default.Person, "PERFIL", Color.Gray, onPerfilClick)
            }
        }

        // Botón central flotante
        Surface(
            shape = CircleShape,
            color = Color(0xFF4A6B4A),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(70.dp)
                .offset(y = (-20).dp)
                .clickable { onFlechaClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Cargar receta",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
    }
}