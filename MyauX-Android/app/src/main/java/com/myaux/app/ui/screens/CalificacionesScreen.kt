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
import com.myaux.app.data.model.Calificacion
import com.myaux.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificacionesScreen(
    calificaciones: List<Calificacion>,
    nombre: String,
    apellido: String,
    numControl: Long,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Mis Calificaciones", fontWeight = FontWeight.Bold) },
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
            // Header info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "$nombre $apellido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "No. Control: $numControl",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            } else if (calificaciones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay calificaciones registradas aún", color = TextGray, fontSize = 15.sp)
                }
            } else {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkTopBar)
                        .padding(12.dp)
                ) {
                    Text("Materia", Modifier.weight(2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Text("P1", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                    Text("P2", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                    Text("P3", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                    Text("Prom", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                    Text("Estado", Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold, textAlign = TextAlign.Center)
                }

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(calificaciones) { index, cal ->
                        val visible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 80L)
                            visible.value = true
                        }

                        AnimatedVisibility(
                            visible = visible.value,
                            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                        ) {
                            CalificacionRow(cal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalificacionRow(cal: Calificacion) {
    val promedioColor = when {
        cal.promedio >= 9.0 -> AccentGreenDark
        cal.promedio >= 8.0 -> AccentGreen
        cal.promedio >= 7.0 -> AccentOrange
        cal.promedio >= 6.0 -> Color(0xFFFF6400)
        else -> AccentRed
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                cal.materia,
                Modifier.weight(2f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                cal.parcial1?.let { String.format("%.1f", it) } ?: "-",
                Modifier.weight(1f),
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Text(
                cal.parcial2?.let { String.format("%.1f", it) } ?: "-",
                Modifier.weight(1f),
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Text(
                cal.parcial3?.let { String.format("%.1f", it) } ?: "-",
                Modifier.weight(1f),
                fontSize = 13.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Text(
                String.format("%.1f", cal.promedio),
                Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = promedioColor,
                textAlign = TextAlign.Center
            )
            Text(
                if (cal.esAprobado) "✅" else "❌",
                Modifier.weight(1.2f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
