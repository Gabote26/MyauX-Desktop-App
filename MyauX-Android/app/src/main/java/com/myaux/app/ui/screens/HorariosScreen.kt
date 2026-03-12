package com.myaux.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myaux.app.ui.theme.*
import androidx.compose.ui.graphics.asImageBitmap

// Data class for schedule key
data class HorarioKey(val semestre: String, val grupo: String, val turno: String)

// Build the schedules map (exactly as in Java)
fun buildHorariosMap(): Map<HorarioKey, Pair<String, String>> {
    val map = mutableMapOf<HorarioKey, Pair<String, String>>()

    fun agregar(sem: String, grupo: String, turno: String, archivo: String) {
        // Determine the folder based on semester and turno type
        val turnoFolder = if (turno.endsWith("M")) "MATU" else "VESP"
        val semFolder = when (sem) {
            "1°" -> "1ER-SEMESTRE-$turnoFolder"
            "3°" -> "3ER-SEMESTRE-$turnoFolder"
            "5°" -> "5TO-SEMESTRE-$turnoFolder"
            else -> ""
        }
        map[HorarioKey(sem, grupo, turno)] = Pair(semFolder, archivo)
    }

    // 1ro matutino
    agregar("1°", "ADMRH", "AM", "1_AMADMRH_page-0001.jpg")
    agregar("1°", "ADMRH", "BM", "1_BMADMRH_page-0001.jpg")
    agregar("1°", "ADMRH", "CM", "1-CMADMRH_page-0001.jpg")
    agregar("1°", "ELE", "AM", "1_AMELE_page-0001.jpg")
    agregar("1°", "MAU", "AM", "1_AMMAU_page-0001.jpg")
    agregar("1°", "MEC", "AM", "1_AMMEC_page-0001.jpg")
    agregar("1°", "PRO", "AM", "1_AMPRO_page-0001.jpg")
    agregar("1°", "SMEC", "AM", "1_AMSMEC_page-0001.jpg")
    agregar("1°", "LOG", "AM", "1-AMLOG_page-0001.jpg")

    // 1ro vespertino
    agregar("1°", "ADMRH", "AV", "1_AVADMRH_page-0001.jpg")
    agregar("1°", "ADMRH", "BV", "1_BVADMRH_page-0001.jpg")
    agregar("1°", "ELE", "AV", "1-AVELE_page-0001.jpg")
    agregar("1°", "MAU", "AV", "1_AVMAU_page-0001.jpg")
    agregar("1°", "MEC", "AV", "1_AVMEC_page-0001.jpg")
    agregar("1°", "MEC", "BV", "1_BVMEC_page-0001.jpg")
    agregar("1°", "PRO", "AV", "1_AVPRO_page-0001.jpg")
    agregar("1°", "SMEC", "AV", "1-AVSMEC_page-0001.jpg")
    agregar("1°", "LOG", "AV", "1_AVLOG_page-0001.jpg")

    // 3ro matutino
    agregar("3°", "ADMRH", "AM", "3_AMADMRH_page-0001.jpg")
    agregar("3°", "ADMRH", "BM", "3_BMAMDRH_page-0001.jpg")
    agregar("3°", "ADMRH", "CM", "3_CMADMRH_page-0001.jpg")
    agregar("3°", "ELE", "AM", "3-AMELE_page-0001.jpg")
    agregar("3°", "MAU", "AM", "3-AMMAU_page-0001.jpg")
    agregar("3°", "MEC", "AM", "3_AMMEC_page-0001.jpg")
    agregar("3°", "PRO", "AM", "3_AMPRO_page-0001.jpg")
    agregar("3°", "SMEC", "AM", "3_AMSMEC_page-0001.jpg")
    agregar("3°", "LOG", "AM", "3_AMLOG_page-0001.jpg")

    // 3ro vespertino
    agregar("3°", "ADMRH", "AV", "3_AVADMRH_page-0001.jpg")
    agregar("3°", "ADMRH", "BV", "3_BVADMRH_page-0001.jpg")
    agregar("3°", "ADMRH", "CV", "3-CVADMRH_page-0001.jpg")
    agregar("3°", "ELE", "AV", "3_AVELE_page-0001.jpg")
    agregar("3°", "MAU", "AV", "3_AVMAU_page-0001.jpg")
    agregar("3°", "MEC", "AV", "3-AVMEC_page-0001.jpg")
    agregar("3°", "PRO", "AV", "3_AVPRO_page-0001.jpg")
    agregar("3°", "SMEC", "AV", "3_AVSMEC_page-0001.jpg")
    agregar("3°", "LOG", "AV", "3_AVLOG_page-0001.jpg")

    // 5to matutino
    agregar("5°", "ADMRH", "AM", "5_AMADMRH_page-0001.jpg")
    agregar("5°", "ADMRH", "BM", "5_BMADMRH_page-0001.jpg")
    agregar("5°", "ADMRH", "CM", "5_CMADMRH_page-0001.jpg")
    agregar("5°", "ELE", "AM", "5_AMELE_page-0001.jpg")
    agregar("5°", "MAU", "AM", "5_AMMAU_page-0001.jpg")
    agregar("5°", "MEC", "AM", "5_AMMEC_page-0001.jpg")
    agregar("5°", "PRO", "AM", "5_AMPRO_page-0001.jpg")
    agregar("5°", "SMEC", "AM", "5_AMSMEC_page-0001.jpg")
    agregar("5°", "LOG", "AM", "5_AMLOG_page-0001.jpg")

    // 5to vespertino
    agregar("5°", "ADMRH", "AV", "5_AVADMHR_page-0001.jpg")
    agregar("5°", "ADMRH", "BV", "5_BVADMRH_page-0001.jpg")
    agregar("5°", "ELE", "AV", "5_AVELE_page-0001.jpg")
    agregar("5°", "MAU", "AV", "5_AVMAU_page-0001.jpg")
    agregar("5°", "MEC", "AV", "5_AVMEC_page-0001.jpg")
    agregar("5°", "PRO", "AV", "5_AVPRO_page-0001.jpg")
    agregar("5°", "SMEC", "AV", "5_AVSMEC_page-0001.jpg")

    return map
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(onBack: () -> Unit) {
    val horariosMap = remember { buildHorariosMap() }

    val semestres = listOf("Seleccione...", "1°", "3°", "5°")
    val grupos = listOf("Seleccione...", "ADMRH", "PRO", "ELE", "MEC", "MAU", "SMEC", "LOG")
    val turnos = listOf("Seleccione...", "AM", "BM", "CM", "AV", "BV", "CV")

    var selectedSemestre by remember { mutableStateOf(semestres[0]) }
    var selectedGrupo by remember { mutableStateOf(grupos[0]) }
    var selectedTurno by remember { mutableStateOf(turnos[0]) }

    var expandedSem by remember { mutableStateOf(false) }
    var expandedGrupo by remember { mutableStateOf(false) }
    var expandedTurno by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Find schedule image
    val horarioResult = remember(selectedSemestre, selectedGrupo, selectedTurno) {
        if (selectedSemestre == "Seleccione..." || selectedGrupo == "Seleccione..." || selectedTurno == "Seleccione...") {
            null
        } else {
            horariosMap[HorarioKey(selectedSemestre, selectedGrupo, selectedTurno)]
        }
    }

    LaunchedEffect(selectedSemestre, selectedGrupo, selectedTurno) {
        errorMessage = if (
            selectedSemestre != "Seleccione..." &&
            selectedGrupo != "Seleccione..." &&
            selectedTurno != "Seleccione..." &&
            horarioResult == null
        ) {
            "No se encontró el horario seleccionado."
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗓️ Horarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3C466E),
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
            // Filters header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Selecciona tu semestre, grupo y turno:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Semestre dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedSem,
                            onExpandedChange = { expandedSem = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedSemestre,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semestre", fontSize = 10.sp) },
                                modifier = Modifier.menuAnchor(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSem) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            ExposedDropdownMenu(expandedSem, { expandedSem = false }) {
                                semestres.forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = {
                                        selectedSemestre = s; expandedSem = false
                                    })
                                }
                            }
                        }

                        // Grupo dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedGrupo,
                            onExpandedChange = { expandedGrupo = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedGrupo,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Grupo", fontSize = 10.sp) },
                                modifier = Modifier.menuAnchor(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedGrupo) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            ExposedDropdownMenu(expandedGrupo, { expandedGrupo = false }) {
                                grupos.forEach { g ->
                                    DropdownMenuItem(text = { Text(g) }, onClick = {
                                        selectedGrupo = g; expandedGrupo = false
                                    })
                                }
                            }
                        }

                        // Turno dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedTurno,
                            onExpandedChange = { expandedTurno = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedTurno,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Turno", fontSize = 10.sp) },
                                modifier = Modifier.menuAnchor(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedTurno) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            ExposedDropdownMenu(expandedTurno, { expandedTurno = false }) {
                                turnos.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = {
                                        selectedTurno = t; expandedTurno = false
                                    })
                                }
                            }
                        }
                    }
                }
            }

            // Image display
            if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = AccentRed, fontSize = 14.sp)
                }
            } else if (horarioResult != null) {
                val (folder, fileName) = horarioResult
                // Display path info and the image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📄 $folder / $fileName",
                            fontSize = 11.sp,
                            color = TextDarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // The image will be loaded from the app's assets
                        // For now, show a placeholder message since images need to be bundled
                        ScheduleImageDisplay(folder = folder, fileName = fileName)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Selecciona semestre, grupo y turno\npara ver tu horario", color = TextGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleImageDisplay(folder: String, fileName: String) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Try to load from assets
    val bitmap = remember(folder, fileName) {
        try {
            val inputStream = context.assets.open("horarios/$folder/$fileName")
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Horario $folder $fileName",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Text(
            "⚠️ Imagen no disponible\n($folder/$fileName)\n\nPara cargar las imágenes de horarios, copia las carpetas de horarios\nal directorio assets/horarios/ del proyecto Android.",
            color = TextGray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}
