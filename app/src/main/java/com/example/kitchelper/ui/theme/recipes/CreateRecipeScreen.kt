package com.example.kitchelper.ui.theme.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.DropdownField
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN
import com.example.kitchelper.ui.theme.home.GuiaInteractivaAR
import com.example.kitchelper.ui.theme.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CreateRecipeScreen(
    navController: NavController,
    recetaIdToEdit: String? = null,
    viewModel: CreateRecipeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val ingredientesAR by (
            savedStateHandle?.getStateFlow<List<String>>("ar_ingredientes", emptyList())
                ?: remember { MutableStateFlow(emptyList()) }
            ).collectAsState()

    LaunchedEffect(ingredientesAR) {
        if (ingredientesAR.isNotEmpty()) {
            viewModel.agregarIngredientesDesdeAR(ingredientesAR)
            savedStateHandle?.remove<List<String>>("ar_ingredientes")
        }
    }

    LaunchedEffect(recetaIdToEdit) {
        if (!recetaIdToEdit.isNullOrBlank()) {
            viewModel.cargarParaEditar(recetaIdToEdit)
        }
    }

    // Notificaciones y navegación al guardar
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            Toast.makeText(context, "¡Receta publicada con éxito!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(state.mensajeExito) {
        state.mensajeExito?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(state.mensajeError) {
        state.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // ===== HEADER DE LA PANTALLA =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { navController.popBackStack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (state.esPracticaBasica) "CREAR PRÁCTICA" else "CREAR RECETA",
                        fontFamily = FrauncesFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (state.esPracticaBasica) "Comparte una técnica o práctica básica" else "Comparte tu receta con la comunidad",
                        fontFamily = InterFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // ===== SELECTOR TIPO DE CREACIÓN =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.esPracticaBasica,
                    onClick = { viewModel.toggleEsPracticaBasica(false) },
                    label = { Text("Receta", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.esPracticaBasica,
                    onClick = { viewModel.toggleEsPracticaBasica(true) },
                    label = { Text("Práctica Básica", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== TARJETA 1: INFORMACIÓN PRINCIPAL =====
            SeccionFormulario(titulo = "Información Principal") {
                CampoTextoFormulario(
                    label = "Título de la Receta",
                    valor = state.titulo,
                    onValorChange = viewModel::actualizarTitulo,
                    placeholder = "Ej. Fettuccine Alfredo Supremo"
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampoTextoFormulario(
                    label = "Descripción",
                    valor = state.descripcion,
                    onValorChange = viewModel::actualizarDescripcion,
                    placeholder = "Resumen de la receta...",
                    singleLine = false,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownField(
                            label = "Categoría",
                            valorSeleccionado = state.categoria,
                            opciones = listOf("Desayuno", "Almuerzo", "Cena", "Postre", "Bebida", "Entrada"),
                            onValorChange = viewModel::actualizarCategoria
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownField(
                            label = "Dificultad",
                            valorSeleccionado = state.dificultad,
                            opciones = listOf("Principiante", "Fácil", "Intermedio", "Media", "Difícil", "Avanzado"),
                            onValorChange = viewModel::actualizarDificultad
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                CampoTextoFormulario(
                    label = "Tiempo de preparación (minutos)",
                    valor = state.tiempoMinutosText,
                    onValorChange = viewModel::actualizarTiempo,
                    placeholder = "15",
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampoTextoFormulario(
                    label = "URL de Video Principal (Opcional)",
                    valor = state.videoPrincipalUrl,
                    onValorChange = viewModel::actualizarVideoPrincipal,
                    placeholder = "https://youtube.com/watch?v=..."
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectorImagenKit(
                    label = "Imagen Principal de la Receta",
                    imagenUrl = state.imagenPrincipalUrl,
                    onImagenSeleccionada = { uri -> viewModel.subirImagenPrincipal(context, uri) },
                    botonTexto = if (state.imagenPrincipalUrl.isNotBlank()) "Cambiar portada en ImageKit.io" else "+ Subir foto de portada a ImageKit.io"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TARJETA 2: INGREDIENTES (CON IMAGEN POR INGREDIENTE) =====
            SeccionFormulario(titulo = "Ingredientes") {
                state.ingredientes.forEach { item ->
                    ItemIngredienteForm(
                        item = item,
                        onNombreChange = { nom -> viewModel.actualizarIngrediente(item.id, nom, item.cantidad) },
                        onCantidadChange = { cant -> viewModel.actualizarIngrediente(item.id, item.nombre, cant) },
                        onImagenSeleccionada = { uri -> viewModel.subirImagenIngrediente(context, uri, item.id) },
                        onEliminar = { viewModel.eliminarIngrediente(item.id) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.agregarIngrediente() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar Ingrediente", fontFamily = InterFamily, fontWeight = FontWeight.Bold)
                }
                if (state.incluyeAR && !state.esPracticaBasica) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate(Routes.AR_SCAN) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Escanear ingredientes con AR", fontFamily = InterFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TARJETA 3: PASOS DE PREPARACIÓN (CON IMAGEN Y VIDEO POR PASO) =====
            SeccionFormulario(titulo = "Pasos de Preparación") {
                state.pasos.forEach { item ->
                    ItemPasoForm(
                        item = item,
                        onDescripcionChange = { desc -> viewModel.actualizarPaso(item.id, desc, item.videoUrl) },
                        onVideoUrlChange = { vid -> viewModel.actualizarPaso(item.id, item.descripcion, vid) },
                        onImagenSeleccionada = { uri -> viewModel.subirImagenPaso(context, uri, item.id) },
                        onEliminar = { viewModel.eliminarPaso(item.id) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.agregarPaso() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar Paso", fontFamily = InterFamily, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TARJETA 4: OPCIONES DE REALIDAD AUMENTADA (AR - OPCIONAL) =====
            SeccionFormulario(titulo = "Experiencia AR / VR (Opcional)") {
                SeccionARForm(
                    incluyeAR = state.incluyeAR,
                    onToggleAR = viewModel::toggleIncluyeAR,
                    instruccionAR = state.instruccionAR,
                    onInstruccionChange = viewModel::actualizarInstruccionAR,
                    modelo3DUrl = state.modelo3DUrlAR,
                    onModelo3DSeleccionado = { uri -> viewModel.subirModelo3DAR(context, uri) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TARJETA 5: ETIQUETAS Y TAGS =====
            SeccionFormulario(titulo = "Etiquetas y Categorías") {
                CampoTextoFormulario(
                    label = "Etiquetas (Separadas por comas)",
                    valor = state.tagsText,
                    onValorChange = viewModel::actualizarTagsText,
                    placeholder = "Ej. italiana, saludable, rápida, gourmet"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== BOTÓN DE PUBLICAR RECETA =====
            Button(
                onClick = { viewModel.guardarReceta() },
                enabled = !state.isSaving && !state.isUploading,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
            ) {
                if (state.isSaving || state.isUploading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        state.uploadProgressMessage ?: "Guardando receta...",
                        fontFamily = InterFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        "Publicar Receta",
                        fontFamily = InterFamily,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
