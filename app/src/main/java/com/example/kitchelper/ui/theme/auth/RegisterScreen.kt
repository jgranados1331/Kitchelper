package com.example.kitchelper.ui.theme.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchenper.ui.auth.AuthViewModel
import com.example.kitchenper.ui.auth.RegisterData
import kotlinx.coroutines.launch

// Número total de pasos
private const val TOTAL_PASOS = 6

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // Estado del formulario
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var mes by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var experticia by remember { mutableStateOf("Principiante") }

    val state by viewModel.authState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { TOTAL_PASOS })
    val scope = rememberCoroutineScope()

    // Efecto: si el registro es exitoso, navegar al Home
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    // AlertDialog para errores
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Error") },
            text = { Text(state.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) { Text("OK") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6EC)) // Fondo crema
    ) {
        // Header con logo y flecha atrás
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (pagerState.currentPage > 0) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color(0xFF4A4A4A)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Aquí iría el logo pequeño de kitchenper
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👨‍🍳", fontSize = 24.sp)
                    }
                }
            }

            // Pager con los pasos
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Evita que el usuario deslice sin validar
            ) { page ->
                when (page) {
                    0 -> Paso1Nombre(
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                    1 -> Paso2Apellido(
                        apellido = apellido,
                        onApellidoChange = { apellido = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
                    )
                    2 -> Paso3EmailPassword(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onConfirmChange = { confirmPassword = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(3) } }
                    )
                    3 -> Paso4FechaNacimiento(
                        dia = dia, mes = mes, anio = anio,
                        onDiaChange = { dia = it },
                        onMesChange = { mes = it },
                        onAnioChange = { anio = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(4) } }
                    )
                    4 -> Paso5Experticia(
                        experticia = experticia,
                        onExperticiaChange = { experticia = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(5) } }
                    )
                    5 -> Paso6Confirmacion(
                        onFinish = {
                            val fechaNacimiento = "$dia/$mes/$anio"
                            viewModel.register(
                                RegisterData(
                                    nombre = nombre,
                                    apellido = apellido,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    fechaNacimiento = fechaNacimiento,
                                    experticia = experticia,
                                )
                            )
                        }
                    )
                }
            }

            // Indicador de pasos (puntitos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(TOTAL_PASOS) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                            .background(
                                color = if (index == pagerState.currentPage) Color(0xFFC5D86D) else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ============ PASO 1: NOMBRE ============
@Composable
fun Paso1Nombre(nombre: String, onNombreChange: (String) -> Unit, onNext: () -> Unit) {
    PasoBase(
        icono = Icons.Default.Person,
        titulo = "¿Cómo te llamas?",
        subtitulo = "Ingresa tu nombre",
        campoValor = nombre,
        onCampoChange = onNombreChange,
        placeholder = "Nombre",
        onNext = onNext,
        nextEnabled = nombre.isNotBlank()
    )
}

// ============ PASO 2: APELLIDO ============
@Composable
fun Paso2Apellido(apellido: String, onApellidoChange: (String) -> Unit, onNext: () -> Unit) {
    PasoBase(
        icono = Icons.Default.Person,
        titulo = "Tu apellido",
        subtitulo = "Ingresa tu apellido",
        campoValor = apellido,
        onCampoChange = onApellidoChange,
        placeholder = "Apellido",
        onNext = onNext,
        nextEnabled = apellido.isNotBlank()
    )
}

// ============ PASO 3: EMAIL Y PASSWORD ============
@Composable
fun Paso3EmailPassword(
    email: String,
    password: String,
    confirmPassword: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Email,
            contentDescription = null,
            tint = Color(0xFF4A4A4A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Datos de acceso",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A)
        )
        Spacer(modifier = Modifier.height(32.dp))

        CampoRegistro(
            valor = email,
            onValorChange = onEmailChange,
            placeholder = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(12.dp))

        CampoRegistro(
            valor = password,
            onValorChange = onPasswordChange,
            placeholder = "Contraseña",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        CampoRegistro(
            valor = confirmPassword,
            onValorChange = onConfirmChange,
            placeholder = "Confirmar contraseña",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true
        )
        Spacer(modifier = Modifier.height(32.dp))

        BotonFlecha(
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            onClick = onNext
        )
    }
}

// ============ PASO 4: FECHA DE NACIMIENTO ============
@Composable
fun Paso4FechaNacimiento(
    dia: String, mes: String, anio: String,
    onDiaChange: (String) -> Unit,
    onMesChange: (String) -> Unit,
    onAnioChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            tint = Color(0xFF4A4A4A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Fecha de nacimiento",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoRegistro(
                valor = dia,
                onValorChange = { if (it.length <= 2) onDiaChange(it) },
                placeholder = "DD",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            CampoRegistro(
                valor = mes,
                onValorChange = { if (it.length <= 2) onMesChange(it) },
                placeholder = "MM",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            CampoRegistro(
                valor = anio,
                onValorChange = { if (it.length <= 4) onAnioChange(it) },
                placeholder = "AAAA",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1.5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BotonFlecha(
            enabled = dia.isNotBlank() && mes.isNotBlank() && anio.length == 4,
            onClick = onNext
        )
    }
}

// ============ PASO 5: EXPERIENCIA ============
@Composable
fun Paso5Experticia(
    experticia: String,
    onExperticiaChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val opciones = listOf("Principiante", "Intermedio", "Avanzado")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Restaurant,
            contentDescription = null,
            tint = Color(0xFF4A4A4A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tu nivel de cocina",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        opciones.forEach { opcion ->
            Card(
                onClick = { onExperticiaChange(opcion) },
                colors = CardDefaults.cardColors(
                    containerColor = if (experticia == opcion) Color(0xFFC5D86D) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    opcion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF4A4A4A),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        BotonFlecha(enabled = true, onClick = onNext)
    }
}

// ============ PASO 6: CONFIRMACIÓN ============
@Composable
fun Paso6Confirmacion(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFC5D86D),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Registro Exitoso",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Presiona para continuar",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        BotonFlecha(enabled = true, onClick = onFinish)
    }
}

// ============ COMPOSABLES AUXILIARES ============

@Composable
fun PasoBase(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    campoValor: String,
    onCampoChange: (String) -> Unit,
    placeholder: String,
    onNext: () -> Unit,
    nextEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = Color(0xFF4A4A4A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            titulo,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitulo,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        CampoRegistro(
            valor = campoValor,
            onValorChange = onCampoChange,
            placeholder = placeholder,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(32.dp))

        BotonFlecha(enabled = nextEnabled, onClick = onNext)
    }
}

@Composable
fun CampoRegistro(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        shape = RoundedCornerShape(24.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFFC5D86D),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BotonFlecha(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC5D86D),
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        ),
        modifier = Modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = "Siguiente",
            tint = if (enabled) Color(0xFF4A4A4A) else Color.White
        )
    }
}