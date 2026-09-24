package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.di.AppContainer
import com.example.core.model.ServiceCategory
import com.example.ui.history.BookingHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.BottomTab
import com.example.ui.notifications.NotificationCenterScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal

@Composable
fun MainAppScreen(
    appContainer: AppContainer,
    onNavigateToCategory: (ServiceCategory) -> Unit,
    onNavigateToService: (String) -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAccountDeletion: () -> Unit,
    onNavigateToPolicies: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }
    val currentUser by appContainer.sessionManager.currentUser.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                BottomTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(text = tab.title) },
                        modifier = Modifier.testTag(tab.testTag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CleankrOrange,
                            selectedTextColor = CleankrOrange,
                            indicatorColor = CleankrOrange.copy(alpha = 0.12f),
                            unselectedIconColor = CleankrSlate,
                            unselectedTextColor = CleankrSlate
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomTab.HOME -> HomeScreen(
                    currentUser = currentUser,
                    serviceRepository = appContainer.serviceRepository,
                    bookingRepository = appContainer.bookingRepository,
                    addressRepository = appContainer.addressRepository,
                    notificationRepository = appContainer.notificationRepository,
                    onCategoryClick = onNavigateToCategory,
                    onServiceClick = onNavigateToService,
                    onBookingClick = onNavigateToBooking,
                    onAddressClick = onNavigateToAddresses,
                    onNotificationsClick = { currentTab = BottomTab.NOTIFICATIONS },
                    onProfileClick = { currentTab = BottomTab.ACCOUNT },
                    onSupportClick = onNavigateToSupport
                )

                BottomTab.BOOKINGS -> BookingHistoryScreen(
                    bookingRepository = appContainer.bookingRepository,
                    onBookingClick = onNavigateToBooking,
                    onExploreServices = { currentTab = BottomTab.HOME }
                )

                BottomTab.NOTIFICATIONS -> NotificationCenterScreen(
                    notificationRepository = appContainer.notificationRepository,
                    onBookingClick = onNavigateToBooking
                )

                BottomTab.ACCOUNT -> ProfileScreen(
                    sessionManager = appContainer.sessionManager,
                    authViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        com.example.ui.auth.AuthViewModel(appContainer.sessionManager)
                    },
                    onNavigateToAddresses = onNavigateToAddresses,
                    onNavigateToSupport = onNavigateToSupport,
                    onNavigateToAccountDeletion = onNavigateToAccountDeletion,
                    onNavigateToPolicies = onNavigateToPolicies,
                    onLogout = onLogout
                )
            }
        }
    }
}
