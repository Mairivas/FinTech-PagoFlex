package com.example.fintechpagoflex.ui

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.fintechpagoflex.ui.theme.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fintechpagoflex.NotificationReceiver
import com.example.fintechpagoflex.data.AppDatabase
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToReminderSetup: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(dao))

    val commitments by viewModel.commitments.collectAsState()
    val totalPending by viewModel.totalPending.collectAsState()
    val nextDueDate by viewModel.nextDueDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") },
                actions = {
                    IconButton(onClick = onNavigateToReminders) {
                        Icon(Icons.Default.Notifications, contentDescription = "Ver Recordatorios")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Hola, Juan Pérez",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Total Pendiente",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatCLP(totalPending),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        nextDueDate?.let { date ->
                            Text(
                                text = "Próximo vencimiento: ${formatDate(date)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Compromisos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (commitments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay compromisos cargados")
                    }
                }
            } else {
                items(commitments, key = { it.id }) { commitment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(commitment.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = commitment.company,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when (commitment.status.lowercase()) {
                                        "pagado" -> StatusSuccessContainer
                                        "vencido" -> StatusWarningContainer
                                        else -> StatusAmberContainer
                                    },
                                    contentColor = when (commitment.status.lowercase()) {
                                        "pagado" -> StatusOnSuccessContainer
                                        "vencido" -> StatusOnWarningContainer
                                        else -> StatusOnAmberContainer
                                    }
                                ) {
                                    Text(
                                        text = commitment.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = commitment.concept,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatCLP(commitment.amount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Vence: ${formatDate(commitment.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToReceipt: (Long) -> Unit
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(dao))

    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historial") })
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay pagos realizados",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(history, key = { it.paymentId }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReceipt(item.paymentId) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.company,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.concept,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatCLP(item.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatDate(item.date),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Método: ${item.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: RecommendViewModel = viewModel(factory = RecommendViewModel.Factory(dao))

    val uiState by viewModel.uiState.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recomendar PagoFlex") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.orgName,
                onValueChange = { viewModel.onOrgNameChanged(it) },
                label = { Text("Nombre de la Organización") },
                leadingIcon = {
                    Icon(Icons.Default.Business, contentDescription = "Organización")
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.orgNameError != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.orgNameError != null) {
                        Text(
                            text = uiState.orgNameError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Column {
                Text("Tipo de Organización")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.orgType == "Colegio",
                        onClick = { viewModel.onOrgTypeChanged("Colegio") }
                    )
                    Text("Colegio")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = uiState.orgType == "Gimnasio",
                        onClick = { viewModel.onOrgTypeChanged("Gimnasio") }
                    )
                    Text("Gimnasio")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.orgType == "Comunidad",
                        onClick = { viewModel.onOrgTypeChanged("Comunidad") }
                    )
                    Text("Comunidad (Gastos Comunes)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = uiState.orgType == "Otro",
                        onClick = { viewModel.onOrgTypeChanged("Otro") }
                    )
                    Text("Otro")
                }
                AnimatedVisibility(visible = uiState.orgTypeError != null) {
                    Text(
                        text = uiState.orgTypeError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = uiState.reason,
                onValueChange = { viewModel.onReasonChanged(it) },
                label = { Text("Razón (Opcional)") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = "Razón")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.submitRecommendation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Recomendación")
            }

            AnimatedVisibility(visible = uiState.isSuccess) {
                Text("Recomendación guardada con éxito", color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Recomendaciones Guardadas", style = MaterialTheme.typography.titleMedium)

            recommendations.forEach { rec ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = rec.organizationName, style = MaterialTheme.typography.bodyLarge)
                        Text(text = rec.type, style = MaterialTheme.typography.bodyMedium)
                        if (rec.reason.isNotBlank()) {
                            Text(text = "Razón: ${rec.reason}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentDetailScreen(
    id: Long,
    onNavigateToSimulatePayment: (Long) -> Unit,
    onNavigateToReminderSetup: (Long) -> Unit,
    onNavigateToReceipt: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(dao))

    val commitment by viewModel.getCommitment(id).collectAsState()
    var photoUriState by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUriState?.let { uri ->
                viewModel.attachPhoto(id, uri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Compromiso") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        commitment?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = data.company, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(text = data.concept, style = MaterialTheme.typography.bodyLarge)
                        Text(text = formatCLP(data.amount), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Estado:", style = MaterialTheme.typography.bodyMedium)
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when (data.status.lowercase()) {
                                    "pagado" -> StatusSuccessContainer
                                    "vencido" -> StatusWarningContainer
                                    else -> StatusAmberContainer
                                },
                                contentColor = when (data.status.lowercase()) {
                                    "pagado" -> StatusOnSuccessContainer
                                    "vencido" -> StatusOnWarningContainer
                                    else -> StatusOnAmberContainer
                                }
                            ) {
                                Text(
                                    text = data.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(text = "Vencimiento: ${formatDate(data.dueDate)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                if (data.photoUri != null) {
                    Text(text = "Comprobante adjunto:", style = MaterialTheme.typography.titleMedium)
                    AsyncImage(
                        model = Uri.parse(data.photoUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                if (data.status.equals("Pendiente", ignoreCase = true) || data.status.equals("Vencido", ignoreCase = true)) {
                    Button(
                        onClick = { onNavigateToSimulatePayment(id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simular pago")
                    }
                } else if (data.status.equals("Pagado", ignoreCase = true)) {
                    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(dao))
                    val paymentItem by historyViewModel.getPaymentByCommitment(id).collectAsState()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Este compromiso ya está pagado", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
                            Button(
                                onClick = {
                                    val receiptId = paymentItem?.paymentId ?: id
                                    onNavigateToReceipt(receiptId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver Comprobante")
                            }
                        }
                    }
                }

                Button(
                    onClick = { onNavigateToReminderSetup(id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar recordatorio")
                }

                Button(
                    onClick = {
                        val file = File(context.cacheDir, "images")
                        if (!file.exists()) file.mkdirs()
                        val imageFile = File(file, "receipt_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
                        photoUriState = uri
                        launcher.launch(uri)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adjuntar foto comprobante")
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptScreen(
    id: Long,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(dao))

    val paymentById by historyViewModel.getPayment(id).collectAsState()
    val paymentByCommitment by historyViewModel.getPaymentByCommitment(id).collectAsState()

    val payment = paymentById ?: paymentByCommitment

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprobante de Pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn() + fadeIn()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "¡Pago Exitoso!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    payment?.let { p ->
                        Text(
                            text = p.company,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = p.concept,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider()
                        Text(
                            text = "Monto: ${formatCLP(p.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fecha de Pago: ${formatDate(p.date)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Método de Pago: ${p.paymentMethod}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    } ?: run {
                        Text(
                            text = "Comprobante N° $id",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SIMULADO — SIN VALIDEZ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatePaymentScreen(
    id: Long,
    onNavigateToReceipt: (Long) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: SimulatePaymentViewModel = viewModel(factory = SimulatePaymentViewModel.Factory(dao))

    val uiState by viewModel.uiState.collectAsState()
    val commitment by viewModel.getCommitment(id).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simular Pago") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        commitment?.let { data ->
            AnimatedContent(
                targetState = uiState.isProcessing,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
                label = "PaymentProcessingTransition",
                modifier = Modifier.padding(padding)
            ) { isProcessing ->
                if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Procesando pago...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confirmando la transacción con ${data.company}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Detalle del pago", style = MaterialTheme.typography.titleMedium)
                                Text(text = data.company, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(text = data.concept, style = MaterialTheme.typography.bodyMedium)
                                Text(text = formatCLP(data.amount), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(text = "Método de Pago", style = MaterialTheme.typography.titleMedium)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = uiState.selectedMethod == "Tarjeta",
                                    onClick = { viewModel.onMethodSelected("Tarjeta") }
                                )
                                Text("Tarjeta de Crédito / Débito")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = uiState.selectedMethod == "Transferencia",
                                    onClick = { viewModel.onMethodSelected("Transferencia") }
                                )
                                Text("Transferencia Bancaria")
                            }
                            AnimatedVisibility(visible = uiState.methodError != null) {
                                Text(
                                    text = uiState.methodError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.onSimulationCheckedChanged(!uiState.isSimulationChecked) }
                            ) {
                                Checkbox(
                                    checked = uiState.isSimulationChecked,
                                    onCheckedChange = { viewModel.onSimulationCheckedChanged(it) }
                                )
                                Text("Confirmo que esto es una simulación")
                            }
                            AnimatedVisibility(visible = uiState.confirmationError != null) {
                                Text(
                                    text = uiState.confirmationError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                viewModel.processSimulatedPayment(data.id, data.amount) { paymentId ->
                                    onNavigateToReceipt(paymentId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar pago simulado")
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: ReminderViewModel = viewModel(factory = ReminderViewModel.Factory(dao))

    val reminders by viewModel.reminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Todos los Recordatorios", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay recordatorios programados",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fecha: ${formatDateTime(reminder.date)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mensaje: ${reminder.message}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        val intent = Intent(context, NotificationReceiver::class.java)
                                        val pendingIntent = PendingIntent.getBroadcast(
                                            context,
                                            reminder.id.toInt(),
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                        )
                                        alarmManager.cancel(pendingIntent)
                                        viewModel.deleteReminder(reminder)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Cancelar o eliminar recordatorio"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancelar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun scheduleNotification(
    context: Context,
    timeInMillis: Long,
    title: String,
    message: String,
    commitmentId: Long,
    reminderId: Int
) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
        putExtra("commitmentId", commitmentId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSetupScreen(
    id: Long,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).appDao()
    val viewModel: ReminderViewModel = viewModel(factory = ReminderViewModel.Factory(dao))

    val uiState by viewModel.uiState.collectAsState()

    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }
    var showSettingsButton by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedMessage = null
            showSettingsButton = false
            pendingAction?.invoke()
            pendingAction = null
        } else {
            permissionDeniedMessage = "Las notificaciones están desactivadas. No recibirás los recordatorios de PagoFlex."
            showSettingsButton = true
            pendingAction = null
        }
    }

    val runWithPermission = { action: () -> Unit ->
        if (Build.VERSION.SDK_INT >= 33) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                permissionDeniedMessage = null
                showSettingsButton = false
                action()
            } else {
                pendingAction = action
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            permissionDeniedMessage = null
            showSettingsButton = false
            action()
        }
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = uiState.dateTimeMillis
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val updatedCalendar = Calendar.getInstance().apply {
                timeInMillis = uiState.dateTimeMillis
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            viewModel.onDateTimeChanged(updatedCalendar.timeInMillis)
        },
        year, month, day
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, min ->
            val updatedCalendar = Calendar.getInstance().apply {
                timeInMillis = uiState.dateTimeMillis
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, min)
            }
            viewModel.onDateTimeChanged(updatedCalendar.timeInMillis)
        },
        hour, minute, true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = formatDateTime(uiState.dateTimeMillis),
                onValueChange = {},
                label = { Text("Fecha y Hora") },
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Fecha y hora")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                isError = uiState.dateTimeError != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.dateTimeError != null) {
                        Text(
                            text = uiState.dateTimeError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Elegir Fecha")
                }
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Elegir Hora")
                }
            }

            OutlinedTextField(
                value = uiState.message,
                onValueChange = { viewModel.onMessageChanged(it) },
                label = { Text("Mensaje") },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Note, contentDescription = "Mensaje")
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.messageError != null,
                supportingText = {
                    AnimatedVisibility(visible = uiState.messageError != null) {
                        Text(
                            text = uiState.messageError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Button(
                onClick = {
                    runWithPermission {
                        viewModel.saveReminder(id) { reminderId, dateTimeMillis, message ->
                            scheduleNotification(
                                context,
                                dateTimeMillis,
                                "PagoFlex Recordatorio",
                                message,
                                id,
                                reminderId.toInt()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Recordatorio")
            }

            if (permissionDeniedMessage != null) {
                Text(
                    text = permissionDeniedMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showSettingsButton) {
                Button(
                    onClick = {
                        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(settingsIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Abrir configuración de la aplicación")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedButton(
                onClick = {
                    runWithPermission {
                        val intent = Intent(context, NotificationReceiver::class.java).apply {
                            putExtra("title", "PagoFlex Prueba")
                            putExtra("message", if (uiState.message.isNotBlank()) uiState.message else "Mensaje de prueba")
                            putExtra("commitmentId", id)
                        }
                        context.sendBroadcast(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar Notificación (Prueba Inmediata)")
            }

            AnimatedVisibility(visible = uiState.isSaved) {
                Text(
                    text = "Recordatorio guardado y programado con éxito",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun formatCLP(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("es-CL"))
    symbols.groupingSeparator = '.'
    formatter.decimalFormatSymbols = symbols
    return "$ ${formatter.format(amount)}"
}

fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CL"))
    return formatter.format(Date(timeInMillis))
}

fun formatDateTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    return formatter.format(Date(timeInMillis))
}