package com.myaux.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaux.app.data.model.Asistencia
import com.myaux.app.data.model.EstadisticasAsistencia
import com.myaux.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciasScreen(
    asistencias: List<Asistencia>,
    estadisticas: EstadisticasAsistencia,
    nombreCompleto: String,
    numControl: Long,
    isLoading: Boolean,
    selectedMateria: String,
    materias: List<String>,
    onMateriaSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Mis Asistencias", fontWeight = FontWeight.Bold) },
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
            // Stats cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("✅", "${estadisticas.presentes}", "Asistencias", AccentGreen, Modifier.weight(1f))
                StatCard("❌", "${estadisticas.faltas}", "Faltas", AccentRed, Modifier.weight(1f))
                StatCard("📝", "${estadisticas.permisos}", "Permisos", AccentYellow, Modifier.weight(1f))
                StatCard("📊", String.format("%.0f%%", estadisticas.porcentaje), "Asistencia", AccentPurple, Modifier.weight(1f))
            }

            // Filter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📚 Filtrar:", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMateria,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .weight(1f)
                                .height(48.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            materias.forEach { materia ->
                                DropdownMenuItem(
                                    text = { Text(materia) },
                                    onClick = {
                                        onMateriaSelected(materia)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            } else if (asistencias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros de asistencias", color = TextGray, fontSize = 15.sp)
                }
            } else {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkTopBar)
                        .padding(12.dp)
                ) {
                    Text("Fecha", Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Text("Materia", Modifier.weight(1.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Text("Estado", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                }

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(asistencias) { index, asistencia ->
                        val visible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            visible.value = true
                        }
                        AnimatedVisibility(
                            visible = visible.value,
                            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 3 })
                        ) {
                            AsistenciaRow(asistencia)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = TextGray)
        }
    }
}

@Composable
private fun AsistenciaRow(asistencia: Asistencia) {
    val bgColor = when (asistencia.estado) {
        "A" -> AccentGreen.copy(alpha = 0.1f)
        "F" -> AccentRed.copy(alpha = 0.1f)
        "P" -> AccentYellow.copy(alpha = 0.1f)
        else -> DarkCard
    }
    val textColor = when (asistencia.estado) {
        "A" -> AccentGreen
        "F" -> AccentRed
        "P" -> AccentYellow
        else -> TextGray
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                asistencia.fecha.toString(),
                Modifier.weight(1.2f),
                fontSize = 13.sp,
                color = Color.White
            )
            Text(
                asistencia.materia,
                Modifier.weight(1.5f),
                fontSize = 13.sp,
                color = Color.White
            )
            Text(
                asistencia.estadoTexto,
                Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
