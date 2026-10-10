package com.example.kitchelper.ui.theme.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recetaId: String, navController: NavController) {
    var receta by remember { mutableStateOf<Receta?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(recetaId) {
        try {
            val doc = FirebaseFirestore.getInstance().collection("recetas").document(recetaId).get().await()
            receta = doc.toObject(Receta::class.java)?.copy(id = doc.id)
        } catch (_: Exception) {}
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6EC))
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4A6B4A))
            }
        } else if (receta == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Receta no encontrada", fontFamily = InterFamily)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        if (receta!!.imagenUrl.isNotBlank()) {
                            AsyncImage(
                                model = receta!!.imagenUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                        }
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.White.copy(alpha = 0.8f), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            receta!!.titulo,
                            fontFamily = InriaSerifFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            receta!!.descripcion,
                            fontFamily = InterFamily,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Ingredientes
                        Text(
                            "Ingredientes",
                            fontFamily = FrauncesFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        receta!!.ingredientes.forEach { ing ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFF4A6B4A))
                                Text("${ing.cantidad} de ${ing.nombre}", fontFamily = InterFamily, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Pasos
                        Text(
                            "Pasos de preparación",
                            fontFamily = FrauncesFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        receta!!.pasos.forEach { paso ->
                            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = Color(0xFF4A6B4A),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${paso.orden}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(paso.descripcion, fontFamily = InterFamily, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
