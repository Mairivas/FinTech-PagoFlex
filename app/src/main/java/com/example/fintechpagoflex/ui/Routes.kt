package com.example.fintechpagoflex.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute {
    val icon: ImageVector
    val label: String
}

@Serializable
data object HomeRoute : TopLevelRoute {
    override val icon = Icons.Default.Home
    override val label = "Inicio"
}

@Serializable
data object HistoryRoute : TopLevelRoute {
    override val icon = Icons.AutoMirrored.Filled.List
    override val label = "Historial"
}

@Serializable
data object RecommendRoute : TopLevelRoute {
    override val icon = Icons.Default.Star
    override val label = "Recomendar"
}

val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(HomeRoute, HistoryRoute, RecommendRoute)

@Serializable
data class CommitmentDetailRoute(val id: Long)

@Serializable
data class PaymentReceiptRoute(val id: Long)

@Serializable
data class SimulatePaymentRoute(val id: Long)

@Serializable
data class ReminderSetupRoute(val id: Long)

@Serializable
data object RemindersRoute

@Serializable
data object ProfileRoute
