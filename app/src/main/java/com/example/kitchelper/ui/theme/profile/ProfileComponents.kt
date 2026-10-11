package com.example.kitchelper.ui.theme.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
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

// Colores del tema
val VERDE_KITCHEN = Color(0xFF4A6B4A)
val VERDE_CLARO = Color(0xFFC5D86D)
val CREMA = Color(0xFFFDF6EC)
val NARANJA = Color(0xFFE07A5F)
val GRIS_TEXTO = Color(0xFF4A4A4A)

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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 12.sp,
            color = GRIS_TEXTO,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEBEBEB),
                unfocusedContainerColor = Color(0xFFEBEBEB),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = GRIS_TEXTO,
                unfocusedTextColor = GRIS_TEXTO
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
            fontSize = 12.sp,
            color = GRIS_TEXTO,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEBEBEB),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { expandido = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        valorSeleccionado.ifBlank { "Selecciona..." },
                        fontFamily = InterFamily,
                        color = GRIS_TEXTO,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Ícono desplegable estilo círculo con flecha hacia abajo
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Desplegar",
                                tint = NARANJA,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false },
                modifier = Modifier.background(Color.White)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, fontFamily = InterFamily, fontSize = 14.sp) },
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
            fontSize = 12.sp,
            color = GRIS_TEXTO,
            fontWeight = FontWeight.Medium
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
                            selectedColor = NARANJA,
                            unselectedColor = Color.Gray
                        )
                    )
                    Text(
                        opcion,
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = GRIS_TEXTO
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
                fontSize = 12.sp,
                color = GRIS_TEXTO,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Botón de más '+' redondo para abrir opciones desplegables
            Box {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEBEBEB),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { mostrarMenu = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar $label",
                            tint = NARANJA,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = { mostrarMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    opcionesComunes.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion, fontFamily = InterFamily, fontSize = 14.sp) },
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
                    placeholder = { Text("Escribe...", fontFamily = InterFamily, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEBEBEB),
                        unfocusedContainerColor = Color(0xFFEBEBEB),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
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
                    colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lista de chips con su botón '-'
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    item,
                    fontFamily = InterFamily,
                    color = NARANJA,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEBEBEB),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEliminar(item) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Eliminar",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
