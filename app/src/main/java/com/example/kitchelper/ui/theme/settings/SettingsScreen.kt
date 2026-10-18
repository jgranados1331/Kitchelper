package com.example.kitchelper.ui.theme.settings

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.DropdownField
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.TarjetaSeccion
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN
import android.content.Context
import android.content.Intent // Permisos App
import android.net.Uri // Permisos App
import android.provider.Settings // Permisos App
import androidx.compose.material.icons.filled.PendingActions

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    var idiomaSeleccionado by remember { mutableStateOf("Español") }
    val modoOscuroActivo by com.example.kitchelper.ui.theme.ThemeManager.isDarkTheme.collectAsState()
    var notificacionesActivas by remember { mutableStateOf(true) }

    var showTerminosDialog by remember { mutableStateOf(false) }
    var showPrivacidadDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CREMA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            // ===== 1. HEADER CONFIGURACIÓN =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = VERDE_KITCHEN,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { navController.popBackStack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Ajustes de la App",
                            fontFamily = InterFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "CONFIGURACIÓN",
                            fontFamily = FrauncesFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VERDE_KITCHEN
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, VERDE_KITCHEN),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { navController.navigate(Routes.PROFILE) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "E",
                            fontFamily = FrauncesFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VERDE_KITCHEN
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 2. TARJETA PREFERENCIAS DE LA APP =====
            TarjetaSeccion(titulo = "Preferencias de la App") {
                // Idioma
                DropdownField(
                    label = "Idioma de la aplicación",
                    valorSeleccionado = idiomaSeleccionado,
                    opciones = listOf("Español", "English"),
                    onValorChange = {
                        idiomaSeleccionado = it
                        Toast.makeText(context, "Idioma guardado: $it", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tema de la app
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = NARANJA,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Tema de la App",
                                fontFamily = InterFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GRIS_TEXTO
                            )
                            Text(
                                if (modoOscuroActivo) "Modo Oscuro" else "Modo Claro",
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = modoOscuroActivo,
                        onCheckedChange = {
                            com.example.kitchelper.ui.theme.ThemeManager.setDarkTheme(it)
                            Toast.makeText(
                                context,
                                if (it) "Modo Oscuro activado" else "Modo Claro activado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VERDE_KITCHEN
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notificaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = NARANJA,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Notificaciones Culinarias",
                                fontFamily = InterFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GRIS_TEXTO
                            )
                            Text(
                                "Recordatorios de recetas y básicos",
                                fontFamily = InterFamily,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = notificacionesActivas,
                        onCheckedChange = {
                            notificacionesActivas = it
                            Toast.makeText(
                                context,
                                if (it) "Notificaciones activadas" else "Notificaciones desactivadas",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VERDE_KITCHEN
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 3. TARJETA PERMISOS DE LA APP =====
            TarjetaSeccion(titulo = "Permisos App") {
                ItemOpcionConfiguracion(
                    icon = Icons.Default.PendingActions ,
                    titulo = "Accede a App Permissions",
                    subtitulo = "Y permitenos usar Archivos, Cámara y GPS",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            // ===== 4. TARJETA INFORMACIÓN Y LEGALES =====
            TarjetaSeccion(titulo = "Información y Legales") {
                ItemOpcionConfiguracion(
                    icon = Icons.Default.Lock,
                    titulo = "Términos y Condiciones",
                    subtitulo = "Condiciones de uso de KitchenPer",
                    onClick = { showTerminosDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ItemOpcionConfiguracion(
                    icon = Icons.Default.PrivacyTip,
                    titulo = "Políticas de Privacidad",
                    subtitulo = "Tratamiento de datos personales",
                    onClick = { showPrivacidadDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ItemOpcionConfiguracion(
                    icon = Icons.Default.Info,
                    titulo = "Acerca de KitchenPer",
                    subtitulo = "Versión v1.0.0 (Build 2026)",
                    onClick = {
                        Toast.makeText(context, "KitchenPer v1.0.0 © 2026", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ===== DIÁLOGO TÉRMINOS Y CONDICIONES =====
        if (showTerminosDialog) {
            AlertDialog(
                onDismissRequest = { showTerminosDialog = false },
                title = { Text("Términos y Condiciones", fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, color = VERDE_KITCHEN) },
                text = {
                    Text(
                        "Bienvenido a KitchenPer. Al hacer uso de nuestra aplicación aceptas utilizar el contenido de recetas, guías interactivas VR/AR y almacenamiento de datos personales con el fin exclusivo de aprendizaje culinario personal.",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showTerminosDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN)
                    ) {
                        Text("Entendido", fontFamily = InterFamily, color = Color.White)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // ===== DIÁLOGO POLÍTICAS DE PRIVACIDAD =====
        if (showPrivacidadDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacidadDialog = false },
                title = { Text("Políticas de Privacidad", fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, color = VERDE_KITCHEN) },
                text = {
                    Text(
                        "En KitchenPer protegemos tu privacidad. Tu correo electrónico, datos de perfil, alergias y recetas se almacenan de manera segura en Firebase Firestore. No compartimos tu información con terceros sin tu consentimiento.",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = GRIS_TEXTO
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPrivacidadDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN)
                    ) {
                        Text("Entendido", fontFamily = InterFamily, color = Color.White)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Barra de navegación inferior flotante
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "profile",
            onInicioClick = { navController.navigate(Routes.HOME) },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}

// Permisos App
fun abrirAjustesPermisosApp(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Composable
fun ItemOpcionConfiguracion(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = NARANJA, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GRIS_TEXTO)
                Text(subtitulo, fontFamily = InterFamily, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
