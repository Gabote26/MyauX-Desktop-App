package com.myaux.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaux.app.data.model.Mensaje
import com.myaux.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajesScreen(
    mensajes: List<Mensaje>,
    unreadCount: Int,
    isLoading: Boolean,
    onMarkRead: (Int) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedMensaje by remember { mutableStateOf<Mensaje?>(null) }

    val filters = listOf("Todos", "Anuncios", "Calificaciones", "Asistencias")

    val filteredMensajes = remember(mensajes, selectedFilter) {
        when (selectedFilter) {
            "Anuncios" -> mensajes.filter { it.tipoMensaje == "anuncio" }
            "Calificaciones" -> mensajes.filter { it.tipoMensaje == "calificacion" }
            "Asistencias" -> mensajes.filter { it.tipoMensaje == "asistencia" }
            else -> mensajes
        }
    }

    // Show message detail
    if (selectedMensaje != null) {
        MensajeDetailScreen(
            mensaje = selectedMensaje!!,
            onBack = { selectedMensaje = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("📬 Mis Mensajes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (unreadCount > 0) {
                            Text(
                                "📬 $unreadCount mensaje(s) sin leer",
                                fontSize = 12.sp,
                                color = AccentGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF34495C),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkCard,
                            labelColor = TextGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            } else if (filteredMensajes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes mensajes", color = TextGray, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(filteredMensajes) { index, mensaje ->
                        val visible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 60L)
                            visible.value = true
                        }
                        AnimatedVisibility(
                            visible = visible.value,
                            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(400))
                        ) {
                            MensajeCard(
                                mensaje = mensaje,
                                onClick = {
                                    onMarkRead(mensaje.id)
                                    selectedMensaje = mensaje
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeCard(mensaje: Mensaje, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mensaje.leido) DarkCard else DarkCard.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (mensaje.leido) 1.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!mensaje.leido) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(top = 6.dp)
                        .then(
                            Modifier.background(AccentBlue, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        mensaje.tipoDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Text(
                        mensaje.fechaEnvio.format(formatter),
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    mensaje.asunto,
                    fontSize = 15.sp,
                    fontWeight = if (mensaje.leido) FontWeight.Normal else FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    mensaje.contenido.take(80) + if (mensaje.contenido.length > 80) "..." else "",
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MensajeDetailScreen(mensaje: Mensaje, onBack: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Mensaje", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF34495C),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Type badge
                    Text(
                        mensaje.tipoDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "📅 ${mensaje.fechaEnvio.format(formatter)}",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Asunto: ${mensaje.asunto}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = DividerColor.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        mensaje.contenido,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
