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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior

// ============ HEADER CON MENÚ DESPLEGABLE ============
@Composable
fun HeaderHome(
    nombreUsuario: String,
    nivelUsuario: String,
    menuExpandido: Boolean,
    onToggleMenu: () -> Unit,
    onPerfilClick: () -> Unit,
    onPuntosClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nivel - $nivelUsuario",
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "KITCHELPER",
                    fontFamily = FrauncesFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Avatar con inicial y menú desplegable
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onToggleMenu() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            nombreUsuario.firstOrNull()?.uppercase() ?: "E",
                            fontFamily = FrauncesFamily,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                // Menú desplegable
                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = onToggleMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("PERFIL", fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onToggleMenu()
                            onPerfilClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("PUNTOS", fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onToggleMenu()
                            onPuntosClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("CONFIGURACIÓN", fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onToggleMenu()
                            onConfiguracionClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("CERRAR SESIÓN", fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE53935)) },
                        onClick = {
                            onToggleMenu()
                            onCerrarSesionClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFE53935))
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Completa tu perfil",
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Queremos estar listos para completar tu información para saber tus gustos y alergias",
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onCompletarClick) {
                Text(
                    "Completar",
                    fontFamily = InterFamily,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============ RECETA DESTACADA (CON INRIA SERIF E IMAGEN CON GRADIENTE DEDIFUMINADO) ============
@Composable
fun RecetaDestacada(receta: Receta, onEmpezar: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4A4A4A),   // quemado: fallback si no hay imagen
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.height(250.dp)) {
            if (receta.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = receta.imagenUrl,
                    contentDescription = receta.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF4A4A4A))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    "RECETA DESTACADA - ${receta.tiempoMinutos} MIN",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFC5D86D),   // quemado: lime acento
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    receta.titulo.ifBlank { "Receta Destacada" },
                    fontFamily = InriaSerifFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    receta.descripcion.ifBlank { "Deliciosa receta lista para cocinar en pocos minutos." },
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onEmpezar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B4A)),   // quemado: naranja consistente
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("EMPEZAR", fontFamily = InterFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ============ GUÍA INTERACTIVA EN VR ============

// Aplicar rutas para mostrar la guia solo cuando el usuario active AR

@Composable
fun GuiaInteractivaAR(onCalibrarClick: () -> Unit, onVistaARClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Guía interactiva en VR",
            fontFamily = FrauncesFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface   // ← único cambio dinámico
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF4A6B4A),   // quemado: mismo verde en ambos temas
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "PASO 1 - DETECCIÓN",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Guía para configuración de VR detecta tu tabla de picar o tu zona de trabajo para poder detectar todos tus cortes",
                    fontFamily = InterFamily,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onCalibrarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5D86D)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("● Calibra tu Guía", fontFamily = InterFamily, color = Color(0xFF4A4A4A), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onVistaARClick) {
                        Text("Vista VR →", fontFamily = InterFamily, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============ SELECTOR DE NIVEL (CONFIGURACIÓN DE DIFICULTAD) ============
@Composable
fun SelectorNivel(
    nivelUsuario: String = "Principiante",
    nivelSeleccionado: String,
    onNivelChange: (String) -> Unit
) {
    val userLevel = nivelUsuario.ifBlank { "Principiante" }
    val niveles = remember(userLevel) {
        listOf(userLevel, "Fácil", "Medio", "Difícil").distinct()
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(niveles) { nivel ->
            FilterChip(
                selected = nivelSeleccionado.equals(nivel, ignoreCase = true),
                onClick = { onNivelChange(nivel) },
                label = { Text(nivel, fontFamily = InterFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (receta.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = receta.imagenUrl,
                        contentDescription = receta.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    receta.titulo,
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${receta.tiempoMinutos} min",
                        fontFamily = InterFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "VR",
                            fontFamily = InterFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
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
        color = MaterialTheme.colorScheme.surface,
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
            if (practica.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = practica.imagenUrl,
                    contentDescription = practica.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    practica.titulo,
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    practica.descripcion,
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "${practica.tiempoMinutos} min",
                        fontFamily = InterFamily,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


