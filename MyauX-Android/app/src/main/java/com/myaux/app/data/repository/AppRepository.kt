package com.myaux.app.data.repository

import com.myaux.app.data.db.DatabaseConnection
import com.myaux.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class AppRepository {

    // =================== LOGIN ===================

    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection()
                ?: return@withContext LoginResult.Error("No se pudo conectar a la base de datos")

            val query = """
                SELECT u.id, u.nombre, u.apellido, u.role, u.no_control, 
                       COALESCE(g.nombre_grupo, 'No asignado') as grupo_nombre
                FROM usuarios u
                LEFT JOIN grupos g ON u.grupo_id = g.id
                WHERE u.email = ? AND u.password = ?
            """

            cn.use { connection ->
                connection.prepareStatement(query).use { ps ->
                    ps.setString(1, email)
                    ps.setString(2, password)
                    val rs = ps.executeQuery()

                    if (rs.next()) {
                        LoginResult.Success(
                            Usuario(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                apellido = rs.getString("apellido"),
                                email = email,
                                role = rs.getString("role"),
                                numControl = rs.getLong("no_control"),
                                grupoNombre = rs.getString("grupo_nombre")
                            )
                        )
                    } else {
                        LoginResult.Error("Usuario o contraseña incorrectos")
                    }
                }
            }
        } catch (e: Exception) {
            LoginResult.Error("Error: ${e.message}")
        }
    }

    suspend fun getUserByEmail(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext null

            val query = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.role, u.no_control,
                       COALESCE(g.nombre_grupo, 'No asignado') as grupo_nombre
                FROM usuarios u
                LEFT JOIN grupos g ON u.grupo_id = g.id
                WHERE u.email = ?
            """

            cn.use { connection ->
                connection.prepareStatement(query).use { ps ->
                    ps.setString(1, email)
                    val rs = ps.executeQuery()

                    if (rs.next()) {
                        Usuario(
                            id = rs.getInt("id"),
                            nombre = rs.getString("nombre"),
                            apellido = rs.getString("apellido"),
                            email = rs.getString("email"),
                            role = rs.getString("role"),
                            numControl = rs.getLong("no_control"),
                            grupoNombre = rs.getString("grupo_nombre")
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== CALIFICACIONES ====================

    suspend fun getCalificaciones(numControl: Long): List<Calificacion> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Calificacion>()
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext lista

            val sql = """
                SELECT id, num_control, materia, parcial_1, parcial_2, parcial_3
                FROM calificaciones
                WHERE num_control = ?
                ORDER BY materia
            """

            cn.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    ps.setLong(1, numControl)
                    val rs = ps.executeQuery()
                    while (rs.next()) {
                        lista.add(
                            Calificacion(
                                id = rs.getInt("id"),
                                numControl = rs.getLong("num_control"),
                                materia = rs.getString("materia"),
                                parcial1 = rs.getObject("parcial_1") as? Double,
                                parcial2 = rs.getObject("parcial_2") as? Double,
                                parcial3 = rs.getObject("parcial_3") as? Double,
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lista
    }

    // ==================== ASISTENCIAS ====================

    suspend fun getEstadisticasAsistencia(numControl: Long): EstadisticasAsistencia = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext EstadisticasAsistencia()

            val sql = """
                SELECT
                    COUNT(*) as total,
                    SUM(CASE WHEN estado = 'A' THEN 1 ELSE 0 END) as presentes,
                    SUM(CASE WHEN estado = 'F' THEN 1 ELSE 0 END) as faltas,
                    SUM(CASE WHEN estado = 'P' THEN 1 ELSE 0 END) as permisos
                FROM asistencias
                WHERE num_control = ?
            """

            cn.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    ps.setLong(1, numControl)
                    val rs = ps.executeQuery()
                    if (rs.next()) {
                        EstadisticasAsistencia(
                            total = rs.getInt("total"),
                            presentes = rs.getInt("presentes"),
                            faltas = rs.getInt("faltas"),
                            permisos = rs.getInt("permisos")
                        )
                    } else EstadisticasAsistencia()
                }
            }
        } catch (e: Exception) {
            EstadisticasAsistencia()
        }
    }

    suspend fun getAsistencias(numControl: Long, materia: String? = null): List<Asistencia> =
        withContext(Dispatchers.IO) {
            val lista = mutableListOf<Asistencia>()
            try {
                val cn = DatabaseConnection.getConnection() ?: return@withContext lista

                val sql = buildString {
                    append("SELECT fecha, materia, estado FROM asistencias WHERE num_control = ?")
                    if (materia != null) append(" AND materia = ?")
                    append(" ORDER BY fecha DESC")
                }

                cn.use { connection ->
                    connection.prepareStatement(sql).use { ps ->
                        ps.setLong(1, numControl)
                        if (materia != null) ps.setString(2, materia)
                        val rs = ps.executeQuery()
                        while (rs.next()) {
                            lista.add(
                                Asistencia(
                                    numControl = numControl,
                                    materia = rs.getString("materia"),
                                    fecha = rs.getDate("fecha").let {
                                        Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate()
                                    },
                                    estado = rs.getString("estado")
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            lista
        }

    // ==================== MENSAJES ====================

    suspend fun getMensajes(usuarioId: Int): List<Mensaje> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Mensaje>()
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext lista

            val sql = """
                SELECT m.id, m.remitente_id, m.tipo_mensaje, m.asunto,
                       m.contenido, m.fecha_envio, md.leido
                FROM mensajes m
                INNER JOIN mensajes_destinatarios md ON m.id = md.mensaje_id
                WHERE md.destinatario_id = ?
                ORDER BY m.fecha_envio DESC
            """

            cn.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    ps.setInt(1, usuarioId)
                    val rs = ps.executeQuery()
                    while (rs.next()) {
                        lista.add(
                            Mensaje(
                                id = rs.getInt("id"),
                                remitenteId = rs.getInt("remitente_id"),
                                tipoMensaje = rs.getString("tipo_mensaje"),
                                asunto = rs.getString("asunto"),
                                contenido = rs.getString("contenido"),
                                fechaEnvio = rs.getTimestamp("fecha_envio").let {
                                    Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
                                },
                                leido = rs.getBoolean("leido")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lista
    }

    suspend fun marcarComoLeido(mensajeId: Int, usuarioId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext false

            val sql = """
                UPDATE mensajes_destinatarios
                SET leido = TRUE, fecha_lectura = NOW()
                WHERE mensaje_id = ? AND destinatario_id = ?
            """

            cn.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    ps.setInt(1, mensajeId)
                    ps.setInt(2, usuarioId)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun contarMensajesNoLeidos(usuarioId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext 0

            val sql = """
                SELECT COUNT(*) as total
                FROM mensajes_destinatarios
                WHERE destinatario_id = ? AND leido = FALSE
            """

            cn.use { connection ->
                connection.prepareStatement(sql).use { ps ->
                    ps.setInt(1, usuarioId)
                    val rs = ps.executeQuery()
                    if (rs.next()) rs.getInt("total") else 0
                }
            }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getUserId(numControl: Long): Int = withContext(Dispatchers.IO) {
        try {
            val cn = DatabaseConnection.getConnection() ?: return@withContext -1

            cn.use { connection ->
                connection.prepareStatement("SELECT id FROM usuarios WHERE no_control = ?").use { ps ->
                    ps.setLong(1, numControl)
                    val rs = ps.executeQuery()
                    if (rs.next()) rs.getInt("id") else -1
                }
            }
        } catch (e: Exception) {
            -1
        }
    }
}
