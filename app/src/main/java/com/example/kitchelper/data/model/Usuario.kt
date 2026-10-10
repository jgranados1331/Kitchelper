package com.example.kitchelper.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "", // Formato "yyyy-MM-dd"
    val perfilCompleto: Boolean = false,
    val informacionCulinaria: InfoCulinaria = InfoCulinaria(),
    val informacionSalud: InfoSalud = InfoSalud(),
    val configuracionApp: ConfiguracionApp = ConfiguracionApp(),
    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class InfoCulinaria(
    val experticia: String = "Principiante", // Principiante, Intermedio, Avanzado
    val nivelSalado: String = "Normal",      // Bajo, Normal, Alto
    val nivelDulce: String = "Normal",
    val preferenciaSabor: String = "Ambas",  // Salado, Dulce, Ambas
    val comidasPreferidas: List<String> = emptyList(),
    val nivelCondimento: String = "Balanceado",
    val saboresPreferidos: List<String> = emptyList(),
    val picantesPermitidos: List<String> = emptyList(),
    val endulzantesPreferidos: List<String> = emptyList()
)

data class InfoSalud(
    val alergias: List<String> = emptyList(),
    val tipoAlimentacion: String = "Normal", // Normal, Fitness, Vegetariano, Vegano
    val problemasSalud: List<String> = emptyList(),
    val consumoAguaLitros: Double = 0.0,
    val restriccionesNutricionales: List<String> = emptyList()
)

data class ConfiguracionApp(
    val terminosAceptados: Boolean = false,
    val fechaAceptacionTerminos: Long = 0,
    val versionTerminos: String = "v1.0",
    val idioma: String = "es",
    val tema: String = "claro",
    val notificaciones: Notificaciones = Notificaciones()
)

data class Notificaciones(
    val nuevasRecetas: Boolean = true,
    val recordatoriosCocinar: Boolean = false
)