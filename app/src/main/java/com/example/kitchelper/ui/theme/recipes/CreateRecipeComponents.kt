package com.example.kitchelper.ui.theme.recipes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN

// ============ SECCIÓN CONTENEDORA ============
@Composable
fun SeccionFormulario(
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

// ============ CAMPO DE TEXTO ============
@Composable
fun CampoTextoFormulario(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
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
            placeholder = { Text(placeholder, fontFamily = InterFamily, fontSize = 13.sp, color = Color.Gray) },
            singleLine = singleLine,
            maxLines = maxLines,
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

// ============ SELECTOR Y CARGADOR DE IMAGEN / ARCHIVO CON IMAGEKIT ============
@Composable
fun SelectorImagenKit(
    label: String,
    imagenUrl: String,
    onImagenSeleccionada: (Uri) -> Unit,
    mimeTypeFilter: String = "image/*",
    botonTexto: String = "Subir a ImageKit.io"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagenSeleccionada(it) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 12.sp,
            color = GRIS_TEXTO,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (imagenUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEBEBEB))
            ) {
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clickable { launcher.launch(mimeTypeFilter) }
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = "Cambiar imagen",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEBEBEB),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launcher.launch(mimeTypeFilter) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = NARANJA,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        botonTexto,
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VERDE_KITCHEN
                    )
                }
            }
        }
    }
}

// ============ ITEM INGREDIENTE EN FORMULARIO ============
@Composable
fun ItemIngredienteForm(
    item: IngredienteItemState,
    onNombreChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onImagenSeleccionada: (Uri) -> Unit,
    onEliminar: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    CampoTextoFormulario(
                        label = "Ingrediente",
                        valor = item.nombre,
                        onValorChange = onNombreChange,
                        placeholder = "Ej. Harina de trigo"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(0.8f)) {
                    CampoTextoFormulario(
                        label = "Cantidad",
                        valor = item.cantidad,
                        onValorChange = onCantidadChange,
                        placeholder = "Ej. 200 g"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar ingrediente",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subida opcional de imagen por ingrediente a ImageKit.io
            SelectorImagenKit(
                label = "Imagen del ingrediente (Opcional)",
                imagenUrl = item.imagenUrl,
                onImagenSeleccionada = onImagenSeleccionada,
                botonTexto = if (item.imagenUrl.isNotBlank()) "Imagen cargada" else "+ Subir foto ingrediente"
            )
        }
    }
}

// ============ ITEM PASO EN FORMULARIO ============
@Composable
fun ItemPasoForm(
    item: PasoItemState,
    onDescripcionChange: (String) -> Unit,
    onVideoUrlChange: (String) -> Unit,
    onImagenSeleccionada: (Uri) -> Unit,
    onEliminar: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Paso #${item.orden}",
                    fontFamily = InriaSerifFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VERDE_KITCHEN
                )
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar paso",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            CampoTextoFormulario(
                label = "Descripción del paso",
                valor = item.descripcion,
                onValorChange = onDescripcionChange,
                placeholder = "Ej. Mezcla la harina con los huevos y bate hasta homogeneizar...",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            SelectorImagenKit(
                label = "Imagen del paso (Opcional)",
                imagenUrl = item.imagenUrl,
                onImagenSeleccionada = onImagenSeleccionada,
                botonTexto = if (item.imagenUrl.isNotBlank()) "Imagen del paso cargada" else "+ Subir foto de este paso"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoFormulario(
                label = "URL Video del paso (Opcional)",
                valor = item.videoUrl,
                onValorChange = onVideoUrlChange,
                placeholder = "https://youtube.com/..."
            )
        }
    }
}

// ============ SECCIÓN OPCIONAL AR ============
@Composable
fun SeccionARForm(
    incluyeAR: Boolean,
    onToggleAR: (Boolean) -> Unit,
    instruccionAR: String,
    onInstruccionChange: (String) -> Unit,
    modelo3DUrl: String,
    onModelo3DSeleccionado: (Uri) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF2F7EC),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = VERDE_KITCHEN,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Guía Interactiva AR / VR",
                        fontFamily = InriaSerifFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VERDE_KITCHEN
                    )
                    Text(
                        "Habilita experiencia 3D en la cocina",
                        fontFamily = InterFamily,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = incluyeAR,
                    onCheckedChange = onToggleAR,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VERDE_KITCHEN)
                )
            }

            if (incluyeAR) {
                Spacer(modifier = Modifier.height(12.dp))
                CampoTextoFormulario(
                    label = "Instrucción de Calibración AR",
                    valor = instruccionAR,
                    onValorChange = onInstruccionChange,
                    placeholder = "Ej. Apunta la cámara a tu tabla de picar..."
                )
                Spacer(modifier = Modifier.height(10.dp))

                SelectorImagenKit(
                    label = "Modelo 3D (.glb / .gltf) a ImageKit.io (Opcional)",
                    imagenUrl = modelo3DUrl,
                    onImagenSeleccionada = onModelo3DSeleccionado,
                    mimeTypeFilter = "*/*",
                    botonTexto = if (modelo3DUrl.isNotBlank()) "Modelo 3D cargado en ImageKit" else "+ Cargar modelo 3D (.glb)"
                )
            }
        }
    }
}
