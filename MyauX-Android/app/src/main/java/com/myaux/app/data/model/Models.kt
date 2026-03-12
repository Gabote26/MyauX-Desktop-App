package com.myaux.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime

// --- User ---
data class Usuario(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val role: String = "",
    val numControl: Long = 0L,
    val grupoNombre: String = "No asignado"
)

// --- Calificacion ---
data class Calificacion(
    val id: Int = 0,
    val numControl: Long = 0L,
    val materia: String = "",
    val parcial1: Double? = null,
    val parcial2: Double? = null,
    val parcial3: Double? = null,
) {
    val promedio: Double
        get() {
            var sum = 0.0
            var count = 0
            parcial1?.let { sum += it; count++ }
            parcial2?.let { sum += it; count++ }
            parcial3?.let { sum += it; count++ }
            return if (count > 0) sum / count else 0.0
        }

    val esAprobado: Boolean get() = promedio >= 6.0
}

// --- Asistencia ---
data class Asistencia(
    val id: Int = 0,
    val numControl: Long = 0L,
    val materia: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val estado: String = "" // "A", "F", "P"
) {
    val estadoTexto: String
        get() = when (estado) {
            "A" -> "✅ Presente"
            "F" -> "❌ Faltó"
            "P" -> "📝 Permiso"
            else -> estado
        }
}

// --- Estadisticas de Asistencia ---
data class EstadisticasAsistencia(
    val total: Int = 0,
    val presentes: Int = 0,
    val faltas: Int = 0,
    val permisos: Int = 0,
) {
    val porcentaje: Double get() = if (total > 0) (presentes * 100.0) / total else 0.0
}

// --- Mensaje ---
data class Mensaje(
    val id: Int = 0,
    val remitenteId: Int = 0,
    val tipoMensaje: String = "",
    val asunto: String = "",
    val contenido: String = "",
    val fechaEnvio: LocalDateTime = LocalDateTime.now(),
    val leido: Boolean = false
) {
    val tipoDisplay: String
        get() = when (tipoMensaje) {
            "anuncio" -> "📣 Anuncio"
            "calificacion" -> "📝 Calificación"
            "asistencia" -> "📋 Asistencia"
            else -> tipoMensaje
        }
}

// --- Login Result ---
sealed class LoginResult {
    data class Success(val usuario: Usuario) : LoginResult()
    data class Error(val message: String) : LoginResult()
    data object Loading : LoginResult()
}
