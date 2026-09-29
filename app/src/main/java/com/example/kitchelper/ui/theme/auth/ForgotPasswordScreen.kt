package com.example.kitchelper.ui.theme.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kitchelper.R
import com.example.kitchenper.ui.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var correoEnviado by remember { mutableStateOf(false) }
    val state by viewModel.authState.collectAsState()

    // Cuando el ViewModel confirma que el correo se envió, cambiamos de pantalla
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            correoEnviado = true
        }
    }

    if (state.isLoading) {
        AuthLoadingDialog(message = state.loadingMessage)
    }

    if (state.errorMessage != null) {
        AuthError(
            message = state.errorMessage!!,
            onDismiss = { viewModel.clearMessages() }
        )
    }

    // ===== FONDO CON IMAGEN =====
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_inicio),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Capa oscura semitransparente para mejor legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // ===== HEADER (transparente sobre el fondo) =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Logo centrado y más grande con la imagen real
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_kitchelper),
                            contentDescription = "Logo Kitchelper",
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Botón atrás alineado a la izquierda
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                ) {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color(0xFF4A4A4A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== TARJETA BLANCA =====
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    if (!correoEnviado) {
                        // ============ PANTALLA 1: INGRESAR CORREO ============
                        PantallaIngresarCorreo(
                            email = email,
                            onEmailChange = { email = it },
                            isLoading = state.isLoading,
                            onEnviar = { viewModel.resetPassword(email) }
                        )
                    } else {
                        // ============ PANTALLA 2: CORREO ENVIADO ============
                        PantallaCorreoEnviado(
                            email = state.emailEnviado ?: email,
                            isLoading = state.isLoading,
                            onReenviar = { viewModel.resetPassword(email) },
                            onVolverLogin = {
                                viewModel.clearMessages()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de derechos reservados
            Text(
                "Derechos reservados©2026",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        if (state.showNoInternet) {
            NoInternetBanner(onDismiss = { viewModel.dismissNoInternet() })
        }
    }
}

// ============ PANTALLA 1: INGRESAR CORREO ============
@Composable
fun PantallaIngresarCorreo(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onEnviar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.Email,
            contentDescription = null,
            tint = Color(0xFF4A4A4A),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Recuperar contraseña",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Correo electrónico", color = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color(0xFFC5D86D),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEnviar,
            enabled = !isLoading && email.isNotBlank(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC5D86D),
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(64.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Enviar",
                tint = if (email.isNotBlank()) Color(0xFF4A4A4A) else Color.White
            )
        }
    }
}

// ============ PANTALLA 2: CORREO ENVIADO ============
@Composable
fun PantallaCorreoEnviado(
    email: String,
    isLoading: Boolean,
    onReenviar: () -> Unit,
    onVolverLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Ícono de éxito (check verde)
        Surface(
            shape = CircleShape,
            color = Color(0xFFC5D86D),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "¡Correo enviado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Revisa tu bandeja de entrada en:",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Correo destacado
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F5F5),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                email,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Sigue el enlace que te enviamos para crear una nueva contraseña. Si no lo ves, revisa tu carpeta de spam.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Reenviar
        OutlinedButton(
            onClick = onReenviar,
            enabled = !isLoading,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4A4A4A)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reenviar correo", fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Volver al Login
        Button(
            onClick = onVolverLogin,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC5D86D)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                "Volver al inicio de sesión",
                color = Color(0xFF4A4A4A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
