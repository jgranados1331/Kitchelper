package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "", // Formato "dd/MM/yyyy"
    val perfilCompleto: Boolean = false,
    val informacionCulinaria: InfoCulinaria = InfoCulinaria(),
    val informacionSalud: InfoSalud = InfoSalud(),
    val configuracionApp: ConfiguracionApp = ConfiguracionApp(),
    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class InfoCulinaria(
    val experticia: String = "Principiante", // Guardado durante el registro
    val nivelSalado: String = "",
    val nivelDulce: String = "",
    val preferenciaComida: String = "",
    val tipoComida: String = "",
    val condimento: String = "",
    val tipoSabor: String = "",
    val picante: String = "",
    val endulzante: String = ""
)

data class InfoSalud(
    val alergias: List<String> = emptyList(),
    val tipoAlimentacion: String = "",
    val problemasSalud: List<String> = emptyList(),
    val consumoAguaLitros: String = "",
    val restricciones: List<String> = emptyList()
)

data class ConfiguracionApp(
    val terminosAceptados: Boolean = true,
    val fechaAceptacionTerminos: Long = System.currentTimeMillis(),
    val versionTerminos: String = "v1.0",
    val idioma: String = "es",
    val tema: String = "claro",
    val notificaciones: Notificaciones = Notificaciones()
)

data class Notificaciones(
    val nuevasRecetas: Boolean = true,
    val recordatoriosCocinar: Boolean = false
)