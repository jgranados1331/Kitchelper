package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Receta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val dificultad: String = "", // Fácil, Media, Difícil
    val tiempoMinutos: Int = 0,
    val categoria: String = "", // Desayuno, Almuerzo, Cena
    val imagenUrl: String = "",
    val videoUrl: String = "",
    val ingredientes: List<Ingrediente> = emptyList(),
    val pasos: List<Paso> = emptyList(),
    val tags: List<String> = emptyList(),
    val autorId: String = "sistema",
    val autorNombre: String = "KitchenPer",
    val esOficial: Boolean = true,
    val estado: String = "aprobada",
    val fechaCreacion: Long = 0,
    val likes: Int = 0,
    val ar: ARData? = null
)

data class Ingrediente(
    val nombre: String = "",
    val cantidad: String = ""
)

data class Paso(
    val orden: Int = 0,
    val descripcion: String = "",
    val imagenUrl: String = ""
)

data class ARData(
    val tieneAR: Boolean = false,
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