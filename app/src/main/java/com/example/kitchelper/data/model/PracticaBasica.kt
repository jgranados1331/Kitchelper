package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class PracticaBasica(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tiempoMinutos: Int = 0,
    val dificultad: String = "Principiante",
    val imagenUrl: String = "",
    val pasos: List<PasoPractica> = emptyList(),
    val erroresComunes: List<String> = emptyList(),
    val ar: ARData? = null
)

data class PasoPractica(
    val orden: Int = 0,
    val descripcion: String = ""
)