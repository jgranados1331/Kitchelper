package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Receta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val dificultad: String = "", // Principiante, Fácil, Intermedio, Media, Difícil, Avanzado
    val tiempoMinutos: Int = 0,
    val categoria: String = "", // Desayuno, Almuerzo, Cena, Postre, Bebida, Entrada
    val imagenUrl: String = "",
    val videoUrl: String = "",
    val ingredientes: List<Ingrediente> = emptyList(),
    val pasos: List<Paso> = emptyList(),
    val tags: List<String> = emptyList(),
    val autorId: String = "sistema",
    val autorNombre: String = "KitchenPer",
    val esOficial: Boolean = false,
    val estado: String = "aprobada",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val ar: ARData? = null
)

data class Ingrediente(
    val nombre: String = "",
    val cantidad: String = "",
    val imagenUrl: String = "" // Imagen opcional por ingrediente (ImageKit.io)
)

data class Paso(
    val orden: Int = 0,
    val descripcion: String = "",
    val imagenUrl: String = "", // Imagen opcional por paso (ImageKit.io)
    val videoUrl: String = ""  // Video opcional por paso
)

data class ARData(
    val tieneAR: Boolean = false,
    val instruccionAR: String = "",
    val modelo3DUrl: String = "", // Modelo 3D (.glb / .gltf) subido a ImageKit.io
    val pasosAR: List<PasoAR> = emptyList()
)

data class PasoAR(
    val orden: Int = 0,
    val instruccion: String = "",
    val duracionSegundos: Int = 0,
    val tipoAnimacion: String = "",
    val modelo3DUrl: String = "",
    val animacionUrl: String = "",
    val posicionInicial: Posicion3D = Posicion3D(),
    val escala: Float = 1.0f,
    val textoOverlay: String = ""
)

data class Posicion3D(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)
