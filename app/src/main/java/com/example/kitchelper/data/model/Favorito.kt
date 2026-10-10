package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favorito(
    val id: String = "",
    val userId: String = "",
    val recetaId: String = "",
    val fechaAgregado: Long = System.currentTimeMillis()
)