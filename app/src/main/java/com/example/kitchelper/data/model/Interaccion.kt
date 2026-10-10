package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Interaccion(
    val id: String = "",
    val userId: String = "",
    val recetaId: String = "",
    val tipo: String = "", // "vista", "cocinado", "favorito"
    val fecha: Long = System.currentTimeMillis()
)