package com.myaux.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myaux.app.data.model.*
import com.myaux.app.data.repository.AppRepository
import com.myaux.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository()
    val sessionManager = SessionManager(application)

    // App state
    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // Current user
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    // Login state
    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState.asStateFlow()

    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    // Calificaciones
    private val _calificaciones = MutableStateFlow<List<Calificacion>>(emptyList())
    val calificaciones: StateFlow<List<Calificacion>> = _calificaciones.asStateFlow()

    private val _calificacionesLoading = MutableStateFlow(false)
    val calificacionesLoading: StateFlow<Boolean> = _calificacionesLoading.asStateFlow()

    // Asistencias
    private val _asistencias = MutableStateFlow<List<Asistencia>>(emptyList())
    val asistencias: StateFlow<List<Asistencia>> = _asistencias.asStateFlow()

    private val _estadisticasAsistencia = MutableStateFlow(EstadisticasAsistencia())
    val estadisticasAsistencia: StateFlow<EstadisticasAsistencia> = _estadisticasAsistencia.asStateFlow()

    private val _asistenciasLoading = MutableStateFlow(false)
    val asistenciasLoading: StateFlow<Boolean> = _asistenciasLoading.asStateFlow()

    private val _selectedMateria = MutableStateFlow("Todas las materias")
    val selectedMateria: StateFlow<String> = _selectedMateria.asStateFlow()

    val materias = listOf("Todas las materias", "Lengua", "Humanidades", "Matematicas", "Sociales", "Ciencias")

    // Mensajes
    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    private val _mensajesLoading = MutableStateFlow(false)
    val mensajesLoading: StateFlow<Boolean> = _mensajesLoading.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val isActive = sessionManager.isSessionActive()
            if (isActive) {
                val email = sessionManager.getEmail()
                if (email != null) {
                    val user = repository.getUserByEmail(email)
                    if (user != null) {
                        _usuario.value = user
                        _appState.value = AppState.Dashboard
                        loadUnreadCount()
                        return@launch
                    }
                }
                sessionManager.cerrarSesion()
            }
            _appState.value = AppState.Login
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginState.value = LoginResult.Loading

            val result = repository.login(email, password)
            _loginState.value = result

            if (result is LoginResult.Success) {
                _usuario.value = result.usuario
                sessionManager.guardarSesion(email)
                _appState.value = AppState.Dashboard
                loadUnreadCount()
            }

            _isLoginLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.cerrarSesion()
            _usuario.value = null
            _loginState.value = null
            _calificaciones.value = emptyList()
            _asistencias.value = emptyList()
            _mensajes.value = emptyList()
            _appState.value = AppState.Login
        }
    }

    fun loadCalificaciones() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            _calificacionesLoading.value = true
            _calificaciones.value = repository.getCalificaciones(user.numControl)
            _calificacionesLoading.value = false
        }
    }

    fun loadAsistencias() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            _asistenciasLoading.value = true
            _estadisticasAsistencia.value = repository.getEstadisticasAsistencia(user.numControl)
            val materia = if (_selectedMateria.value == "Todas las materias") null else _selectedMateria.value
            _asistencias.value = repository.getAsistencias(user.numControl, materia)
            _asistenciasLoading.value = false
        }
    }

    fun onMateriaSelected(materia: String) {
        _selectedMateria.value = materia
        loadAsistencias()
    }

    fun loadMensajes() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            _mensajesLoading.value = true
            val userId = repository.getUserId(user.numControl)
            if (userId > 0) {
                _mensajes.value = repository.getMensajes(userId)
                _unreadCount.value = repository.contarMensajesNoLeidos(userId)
            }
            _mensajesLoading.value = false
        }
    }

    fun markMessageRead(messageId: Int) {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            val userId = repository.getUserId(user.numControl)
            if (userId > 0) {
                repository.marcarComoLeido(messageId, userId)
                // Update local state
                _mensajes.value = _mensajes.value.map {
                    if (it.id == messageId) it.copy(leido = true) else it
                }
                _unreadCount.value = repository.contarMensajesNoLeidos(userId)
            }
        }
    }

    private fun loadUnreadCount() {
        val user = _usuario.value ?: return
        viewModelScope.launch {
            val userId = repository.getUserId(user.numControl)
            if (userId > 0) {
                _unreadCount.value = repository.contarMensajesNoLeidos(userId)
            }
        }
    }
}

sealed class AppState {
    data object Loading : AppState()
    data object Splash : AppState()
    data object Login : AppState()
    data object Dashboard : AppState()
}
