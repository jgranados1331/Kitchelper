package com.example.kitchelper.ui.theme.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
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
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Interceptar el botón físico/gesto de atrás en Android
    BackHandler {
        viewModel.solicitarSalida(rutaDestino = null)
    }

    // Efecto para navegar cuando la cuenta se elimina con éxito
    LaunchedEffect(state.cuentaEliminadaExitosamente) {
        if (state.cuentaEliminadaExitosamente) {
            Toast.makeText(context, "Tu cuenta ha sido eliminada con éxito.", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Efecto para procesar navegación tras confirmaciones en alertas de salida
    LaunchedEffect(state.debeNavegar) {
        if (state.debeNavegar) {
            viewModel.resetNavegacion()
            val ruta = state.pendingNavigationRoute
            if (ruta != null) {
                navController.navigate(ruta) {
                    popUpTo(Routes.PROFILE) { inclusive = true }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    // Snackbar para mensajes de alerta / estado
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CREMA)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = VERDE_KITCHEN,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 110.dp)
            ) {
                // ===== HEADER =====
                HeaderPerfil(
                    onBackClick = { viewModel.solicitarSalida(rutaDestino = null) },
                    state = state
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ===== TARJETA 1: INFORMACIÓN PERSONAL =====
                TarjetaSeccion(titulo = "Información personal") {
                    CampoTexto(
                        label = "Nombres",
                        valor = state.usuario.nombre,
                        onValorChange = viewModel::actualizarNombre
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampoTexto(
                        label = "Apellidos",
                        valor = state.usuario.apellido,
                        onValorChange = viewModel::actualizarApellido
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampoTexto(
                        label = "Correo Electrónico",
                        valor = state.usuario.email,
                        onValorChange = viewModel::actualizarEmail,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampoTexto(
                        label = "Número Telefónico",
                        valor = state.usuario.telefono,
                        onValorChange = viewModel::actualizarTelefono,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampoTexto(
                        label = "Fecha de Nacimiento",
                        valor = state.usuario.fechaNacimiento,
                        onValorChange = viewModel::actualizarFechaNacimiento
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== TARJETA 2: INFORMACIÓN CULINARIA =====
                TarjetaSeccion(titulo = "Información culinaria") {
                    val info = state.usuario.informacionCulinaria

                    DropdownField(
                        label = "Experticie",
                        valorSeleccionado = info.experticia,
                        opciones = listOf("Principiante", "Intermedio", "Avanzado"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(experticia = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Nivel Salado",
                        valorSeleccionado = info.nivelSalado,
                        opciones = listOf("Bajo", "Normal", "Alto"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(nivelSalado = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Nivel Dulce",
                        valorSeleccionado = info.nivelDulce,
                        opciones = listOf("Bajo", "Normal", "Alto"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(nivelDulce = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ChipsSeleccionUnica(
                        label = "Preferencias Comida",
                        opciones = listOf("Salado", "Dulce", "Ambas"),
                        seleccionado = info.preferenciaComida,
                        onSeleccionChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(preferenciaComida = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Tipo de Comida",
                        valorSeleccionado = info.tipoComida,
                        opciones = listOf("Variada", "Fitness", "Vegetariana", "Vegana"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(tipoComida = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Condimento",
                        valorSeleccionado = info.condimento,
                        opciones = listOf("Bajo", "Balanceado", "Alto"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(condimento = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Tipo de sabor",
                        valorSeleccionado = info.tipoSabor,
                        opciones = listOf("Dulce", "Salado", "Ácido", "Amargo", "Umami"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(tipoSabor = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Picante",
                        valorSeleccionado = info.picante,
                        opciones = listOf("No", "Poco", "Medio", "Mucho"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(picante = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Endulzante",
                        valorSeleccionado = info.endulzante,
                        opciones = listOf("Azúcar", "Panela", "Stevia", "Miel", "Ninguno"),
                        onValorChange = {
                            viewModel.actualizarInfoCulinaria(info.copy(endulzante = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== TARJETA 3: INFORMACIÓN DE SALUD =====
                TarjetaSeccion(titulo = "Información de Salud") {
                    val salud = state.usuario.informacionSalud

                    ListaDinamicaDesplegable(
                        label = "Alergias",
                        items = salud.alergias,
                        opcionesComunes = OPCIONES_ALERGIAS,
                        onAgregar = {
                            viewModel.actualizarInfoSalud(salud.copy(alergias = salud.alergias + it))
                        },
                        onEliminar = {
                            viewModel.actualizarInfoSalud(salud.copy(alergias = salud.alergias - it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Tipo de alimentación",
                        valorSeleccionado = salud.tipoAlimentacion,
                        opciones = listOf("Normal", "Fitness", "Vegetariano", "Vegano"),
                        onValorChange = {
                            viewModel.actualizarInfoSalud(salud.copy(tipoAlimentacion = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ListaDinamicaDesplegable(
                        label = "Problemas de Salud",
                        items = salud.problemasSalud,
                        opcionesComunes = OPCIONES_PROBLEMAS_SALUD,
                        onAgregar = {
                            viewModel.actualizarInfoSalud(salud.copy(problemasSalud = salud.problemasSalud + it))
                        },
                        onEliminar = {
                            viewModel.actualizarInfoSalud(salud.copy(problemasSalud = salud.problemasSalud - it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "Consumo de agua * Día",
                        valorSeleccionado = salud.consumoAguaLitros.ifBlank { "6 litros" },
                        opciones = listOf("1 litro", "2 litros", "3 litros", "4 litros", "5 litros", "6 litros", "Más de 6 litros"),
                        onValorChange = {
                            viewModel.actualizarInfoSalud(salud.copy(consumoAguaLitros = it))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ListaDinamicaDesplegable(
                        label = "Restricciones",
                        items = salud.restricciones,
                        opcionesComunes = OPCIONES_RESTRICCIONES,
                        onAgregar = {
                            viewModel.actualizarInfoSalud(salud.copy(restricciones = salud.restricciones + it))
                        },
                        onEliminar = {
                            viewModel.actualizarInfoSalud(salud.copy(restricciones = salud.restricciones - it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== BOTÓN GUARDAR INFORMACIÓN =====
                Button(
                    onClick = { viewModel.solicitarGuardar() },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Guardar Información",
                            fontFamily = InterFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ===== BOTÓN ELIMINAR CUENTA =====
                OutlinedButton(
                    onClick = { viewModel.solicitarEliminarCuenta() },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE53935)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = "Eliminar Cuenta",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Eliminar Cuenta",
                        fontFamily = InterFamily,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ===== 1. ALERTA: PERFIL INCOMPLETO Y DESEA SALIR =====
        if (state.showDialogSalirIncompleto) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarSalida() },
                title = {
                    Text(
                        "Perfil incompleto",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO
                    )
                },
                text = {
                    Text(
                        "¿Deseas regresar sin completar el perfil?",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmarSalida() },
                        colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN)
                    ) {
                        Text("Continuar", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.cancelarSalida() }
                    ) {
                        Text("Volver", fontFamily = InterFamily, color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // ===== 2. ALERTA: PERFIL COMPLETADO CON CAMBIOS SIN GUARDAR =====
        if (state.showDialogCambiosSinGuardar) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarSalida() },
                title = {
                    Text(
                        "Cambios sin guardar",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO
                    )
                },
                text = {
                    Text(
                        "¿Deseas continuar sin guardar los cambios?",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmarSalida() },
                        colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN)
                    ) {
                        Text("Continuar", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.cancelarSalida() }
                    ) {
                        Text("Volver", fontFamily = InterFamily, color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // ===== 3. ALERTA: CONFIRMAR GUARDAR CAMBIOS =====
        if (state.showDialogConfirmarGuardar) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarGuardar() },
                title = {
                    Text(
                        "Guardar información",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas guardar los cambios?",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.guardarPerfil() },
                        colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN)
                    ) {
                        Text("Continuar", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.cancelarGuardar() }
                    ) {
                        Text("Volver", fontFamily = InterFamily, color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // ===== 4. ALERTA: CONFIRMAR ELIMINAR CUENTA =====
        if (state.showDialogConfirmarEliminarCuenta) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarEliminarCuenta() },
                title = {
                    Text(
                        "Eliminar Cuenta",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar tu cuenta? Esta acción borrará permanentemente toda tu información, perfil y favoritos, y no se podrá deshacer.",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.eliminarCuenta() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Eliminar", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.cancelarEliminarCuenta() }
                    ) {
                        Text("Cancelar", fontFamily = InterFamily, color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Barra de navegación inferior flotante con interceptor de salida
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "profile",
            onInicioClick = { viewModel.solicitarSalida(Routes.HOME) },
            onRecetasClick = { viewModel.solicitarSalida(Routes.RECIPES) },
            onGuardadosClick = { viewModel.solicitarSalida(Routes.FAVORITOS) },
            onPerfilClick = { /* Ya estamos en Perfil */ },
            onFlechaClick = { viewModel.solicitarSalida(Routes.CREATE_RECIPE) }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ============ HEADER DEL PERFIL ============
@Composable
fun HeaderPerfil(onBackClick: () -> Unit, state: ProfileState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Barra superior con flecha de retorno
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = VERDE_KITCHEN,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onBackClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    "Nivel - ${state.usuario.informacionCulinaria.experticia.ifBlank { "Principiante" }}",
                    fontFamily = InterFamily,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    "TU PERFIL",
                    fontFamily = FrauncesFamily,
                    color = GRIS_TEXTO,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            // Avatar circular superior
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        state.usuario.nombre.firstOrNull()?.uppercase() ?: "E",
                        fontFamily = FrauncesFamily,
                        color = VERDE_KITCHEN,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Avatar principal con @Nombre y @Correo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        state.usuario.nombre.firstOrNull()?.uppercase() ?: "E",
                        fontFamily = InriaSerifFamily,
                        color = VERDE_KITCHEN,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "@${state.usuario.nombre.ifBlank { "Nombre" }}",
                    fontFamily = InriaSerifFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VERDE_KITCHEN
                )
                Text(
                    "@${state.usuario.email.ifBlank { "Correo Electrónico" }}",
                    fontFamily = InriaSerifFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VERDE_KITCHEN
                )
            }
        }
    }
}

// ============ TARJETA DE SECCIÓN ============
@Composable
fun TarjetaSeccion(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                titulo,
                fontFamily = InriaSerifFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NARANJA
            )
            Spacer(modifier = Modifier.height(12.dp))
            contenido()
        }
    }
}
