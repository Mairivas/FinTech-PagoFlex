package com.example.fintechpagoflex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.fintechpagoflex.data.AppDatabase
import com.example.fintechpagoflex.ui.CommitmentDetailRoute
import com.example.fintechpagoflex.ui.CommitmentDetailScreen
import com.example.fintechpagoflex.ui.HistoryRoute
import com.example.fintechpagoflex.ui.HistoryScreen
import com.example.fintechpagoflex.ui.HomeRoute
import com.example.fintechpagoflex.ui.HomeScreen
import com.example.fintechpagoflex.ui.PaymentReceiptRoute
import com.example.fintechpagoflex.ui.PaymentReceiptScreen
import com.example.fintechpagoflex.ui.RecommendRoute
import com.example.fintechpagoflex.ui.RecommendScreen
import com.example.fintechpagoflex.ui.ReminderSetupRoute
import com.example.fintechpagoflex.ui.ReminderSetupScreen
import com.example.fintechpagoflex.ui.RemindersRoute
import com.example.fintechpagoflex.ui.RemindersScreen
import com.example.fintechpagoflex.ui.SimulatePaymentRoute
import com.example.fintechpagoflex.ui.SimulatePaymentScreen
import com.example.fintechpagoflex.ui.TOP_LEVEL_ROUTES
import com.example.fintechpagoflex.ui.TopLevelBackStack
import com.example.fintechpagoflex.ui.theme.FinTechPAGOFLEXTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val pendingCommitmentId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDatabase.getDatabase(this)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            FinTechPAGOFLEXTheme {
                val topLevelBackStack = remember { TopLevelBackStack<Any>(HomeRoute) }
                val targetCommitmentId by pendingCommitmentId.collectAsState()

                LaunchedEffect(targetCommitmentId) {
                    val id = targetCommitmentId
                    if (id != null && id != -1L) {
                        topLevelBackStack.add(CommitmentDetailRoute(id))
                        pendingCommitmentId.value = null
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                val isSelected = topLevelRoute == topLevelBackStack.topLevelKey
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        topLevelBackStack.addTopLevel(topLevelRoute)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = topLevelRoute.icon,
                                            contentDescription = null
                                        )
                                    },
                                    label = {
                                        Text(topLevelRoute.label)
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavDisplay(
                        backStack = topLevelBackStack.backStack,
                        onBack = {
                            if (!topLevelBackStack.removeLast()) {
                                finish()
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                        entryProvider = entryProvider {
                            entry<HomeRoute> {
                                HomeScreen(
                                    onNavigateToDetail = { id -> topLevelBackStack.add(CommitmentDetailRoute(id)) },
                                    onNavigateToReminders = { topLevelBackStack.add(RemindersRoute) },
                                    onNavigateToReminderSetup = { id -> topLevelBackStack.add(ReminderSetupRoute(id)) }
                                )
                            }
                            entry<HistoryRoute> {
                                HistoryScreen(
                                    onNavigateToReceipt = { id -> topLevelBackStack.add(PaymentReceiptRoute(id)) }
                                )
                            }
                            entry<RecommendRoute> {
                                RecommendScreen()
                            }
                            entry<CommitmentDetailRoute> { route ->
                                CommitmentDetailScreen(
                                    id = route.id,
                                    onNavigateToSimulatePayment = { id -> topLevelBackStack.add(SimulatePaymentRoute(id)) },
                                    onNavigateToReminderSetup = { id -> topLevelBackStack.add(ReminderSetupRoute(id)) },
                                    onNavigateToReceipt = { paymentId -> topLevelBackStack.add(PaymentReceiptRoute(paymentId)) },
                                    onNavigateBack = {
                                        if (!topLevelBackStack.removeLast()) finish()
                                    }
                                )
                            }
                            entry<PaymentReceiptRoute> { route ->
                                PaymentReceiptScreen(
                                    id = route.id,
                                    onNavigateBack = {
                                        if (!topLevelBackStack.removeLast()) finish()
                                    }
                                )
                            }
                            entry<SimulatePaymentRoute> { route ->
                                SimulatePaymentScreen(
                                    id = route.id,
                                    onNavigateToReceipt = { paymentId -> topLevelBackStack.add(PaymentReceiptRoute(paymentId)) },
                                    onNavigateBack = {
                                        if (!topLevelBackStack.removeLast()) finish()
                                    }
                                )
                            }
                            entry<ReminderSetupRoute> { route ->
                                ReminderSetupScreen(
                                    id = route.id,
                                    onNavigateBack = {
                                        if (!topLevelBackStack.removeLast()) finish()
                                    }
                                )
                            }
                            entry<RemindersRoute> {
                                RemindersScreen(
                                    onNavigateBack = {
                                        if (!topLevelBackStack.removeLast()) finish()
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra("commitmentId")) {
            val commitmentId = intent.getLongExtra("commitmentId", -1L)
            if (commitmentId != -1L) {
                pendingCommitmentId.value = commitmentId
            }
        }
    }
}