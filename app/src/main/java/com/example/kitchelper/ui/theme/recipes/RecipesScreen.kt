package com.example.kitchelper.ui.theme.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val categorias = listOf("Todas", "Desayuno", "Almuerzo", "Cena")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6EC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Recetario",
                fontFamily = FrauncesFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Barra de búsqueda
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.buscarRecetas(it) },
                placeholder = { Text("Buscar recetas, ingredientes...", fontFamily = InterFamily) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF4A6B4A),
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros de categoría
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    FilterChip(
                        selected = state.categoriaSeleccionada == cat,
                        onClick = { viewModel.filtrarPorCategoria(cat) },
                        label = { Text(cat, fontFamily = InterFamily) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A6B4A),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4A6B4A))
                }
            } else if (state.recetasFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron recetas", fontFamily = InterFamily, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.recetasFiltradas) { receta ->
                        RecipeCardItem(receta = receta) {
                            navController.navigate("recipe_detail/${receta.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCardItem(receta: Receta, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (receta.imagenUrl.isNotBlank()) {
                AsyncImage(
                    model = receta.imagenUrl,
                    contentDescription = receta.titulo,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    receta.titulo,
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF4A4A4A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    receta.descripcion,
                    fontFamily = InterFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF0F0F0)
                    ) {
                        Text(
                            "${receta.tiempoMinutos} min",
                            fontFamily = InterFamily,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F0D6)
                    ) {
                        Text(
                            receta.dificultad.ifEmpty { "Fácil" },
                            fontFamily = InterFamily,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF4A6B4A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
