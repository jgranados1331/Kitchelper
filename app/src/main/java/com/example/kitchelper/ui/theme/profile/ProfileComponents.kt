package com.example.kitchelper.ui.theme.profile

import androidx.compose.foundation.background // en desuso por que arraglaron colores quemados
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchelper.ui.theme.InterFamily

// Colores dinámicos del tema según modo Claro / Oscuro
val VERDE_KITCHEN @Composable get() = MaterialTheme.colorScheme.primary
val VERDE_CLARO @Composable get() = Color(0xFFC5D86D) // en desuso por color quemado
val CREMA @Composable get() = MaterialTheme.colorScheme.background
val NARANJA @Composable get() = MaterialTheme.colorScheme.secondary
val GRIS_TEXTO @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val NEGRO_OPCIONES @Composable get() = MaterialTheme.colorScheme.onSurface

// Listas de opciones comunes para salud
val OPCIONES_ALERGIAS = listOf(
    "Almendras", "Cacahuetes", "Mariscos", "Lactosa", "Gluten",
    "Huevo", "Soya", "Pescado", "Frutos secos", "Sésamo", "Mostaza", "Otra..."
)

val OPCIONES_PROBLEMAS_SALUD = listOf(
    "Tensión Alta", "Diabetes", "Gastritis", "Colesterol Alto",
    "Triglicéridos Altos", "Ácido Úrico", "Colitis", "Hepatopatía", "Otra..."
)

val OPCIONES_RESTRICCIONES = listOf(
    "Carbohidratos", "Sodio / Sal", "Azúcar", "Grasas saturadas",
    "Calorías", "Lácteos", "Carnes rojas", "Ultraprocesados", "Alcohol", "Otra..."
)

// ============ CAMPO DE TEXTO SIMPLE ============
@Composable
fun CampoTexto(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                fontFamily = InterFamily,
                fontSize = 13.sp,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            if (!enabled) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Protegido",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            enabled = enabled,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedTextColor = NEGRO_OPCIONES,
                unfocusedTextColor = NEGRO_OPCIONES,
                disabledTextColor = Color.DarkGray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============ DROPDOWN DESPLEGABLE ============
@Composable
fun DropdownField(
    label: String,
    valorSeleccionado: String,
    opciones: List<String>,
    onValorChange: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { expandido = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        valorSeleccionado.ifBlank { "Selecciona una opción..." },
                        fontFamily = InterFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Ícono desplegable
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Desplegar",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false },

            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                opcion,
                                fontFamily = InterFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onValorChange(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

// ============ GRUPO DE CHIPS PARA SELECCIÓN ÚNICA ============
@Composable
fun ChipsSeleccionUnica(
    label: String,
    opciones: List<String>,
    seleccionado: String,
    onSeleccionChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSeleccionChange(opcion) }
                ) {
                    RadioButton(
                        selected = seleccionado.equals(opcion, ignoreCase = true),
                        onClick = { onSeleccionChange(opcion) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        opcion,
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ============ LISTA DINÁMICA CON DESPLEGABLE DE OPCIONES COMUNES ============
@Composable
fun ListaDinamicaDesplegable(
    label: String,
    items: List<String>,
    opcionesComunes: List<String>,
    onAgregar: (String) -> Unit,
    onEliminar: (String) -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarInputOtro by remember { mutableStateOf(false) }
    var otroTexto by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                fontFamily = InterFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Botón + redondo
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { mostrarMenu = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar $label",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false },
                ) {
                    opcionesComunes.forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    opcion,
                                    fontFamily = InterFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                mostrarMenu = false
                                if (opcion == "Otra...") {
                                    mostrarInputOtro = true
                                } else {
                                    if (!items.contains(opcion)) {
                                        onAgregar(opcion)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Input si el usuario selecciona "Otra..."
        if (mostrarInputOtro) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = otroTexto,
                    onValueChange = { otroTexto = it },
                    placeholder = { Text("Escribe...", fontFamily = InterFamily, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (otroTexto.isNotBlank()) {
                            onAgregar(otroTexto.trim())
                            otroTexto = ""
                            mostrarInputOtro = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lista de chips agregados
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Text(
                    item,
                    fontFamily = InterFamily,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onEliminar(item) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
