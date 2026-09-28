package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.core.di.AppContainer
import com.example.core.model.Booking
import com.example.ui.address.AddressManagementScreen
import com.example.ui.admin.AdminHubScreen
import com.example.ui.booking.BookingFlowScreen
import com.example.ui.booking.BookingSuccessScreen
import com.example.ui.components.CleankrStrings
import com.example.ui.components.CleankrTopAppBar
import com.example.ui.history.BookingHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.Screen
import com.example.ui.notifications.NotificationCenterScreen
import com.example.ui.profile.LegalDocumentScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.services.ServiceCatalogScreen
import com.example.ui.services.ServiceDetailScreen
import com.example.ui.support.HelpSupportScreen
import com.example.ui.tracking.BookingTrackingScreen
import com.example.ui.theme.CleankrCardSurface
import com.example.ui.theme.CleankrNavy
import com.example.ui.theme.CleankrTeal

@Composable
fun MainAppScreen(
    container: AppContainer,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userProfile by container.sessionManager.userProfile.collectAsState()
    val currentLanguage = userProfile.language

    val services by container.serviceRepository.observeAllServices().collectAsState(initial = container.firebaseBackend.defaultServicesCatalog)
    val addresses by container.addressRepository.observeAddresses().collectAsState(initial = emptyList())
    val bookings by container.bookingRepository.observeCustomerBookings(userProfile.uid).collectAsState(initial = emptyList())
    val activeBookings = remember(bookings) { bookings.filter { it.status !in listOf("COMPLETED", "CANCELLED") } }
    val notifications by container.notificationRepository.observeNotifications().collectAsState(initial = emptyList())
    val unreadNotificationsCount = remember(notifications) { notifications.count { !it.isRead } }
    val supportTickets by container.supportRepository.observeTickets().collectAsState(initial = emptyList())
    val crossHubAttempts by container.adminHubRepository.observeAllAttempts().collectAsState(initial = emptyList())
    val unresolvedAlertsCount = remember(crossHubAttempts) { crossHubAttempts.count { it.status == "NEW_ALERT" } }

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home.route,
        Screen.Services.route,
        Screen.Bookings.route,
        Screen.Profile.route
    )

    val currentTitle = when (currentRoute) {
        Screen.Home.route -> CleankrStrings.get("app_title", currentLanguage)
        Screen.Services.route -> CleankrStrings.get("services", currentLanguage)
        Screen.Bookings.route -> CleankrStrings.get("bookings", currentLanguage)
        Screen.Profile.route -> CleankrStrings.get("profile", currentLanguage)
        Screen.Addresses.route -> "Saved Addresses"
        Screen.Support.route -> "Help & Support"
        Screen.Notifications.route -> "Notifications"
        else -> "Cleankr"
    }

    Scaffold(
        topBar = {
            if (isTopLevelDestination || currentRoute in listOf(Screen.Addresses.route, Screen.Support.route, Screen.Notifications.route)) {
                CleankrTopAppBar(
                    title = currentTitle,
                    canNavigateBack = !isTopLevelDestination,
                    onNavigateBack = { navController.popBackStack() },
                    currentLanguage = currentLanguage,
                    onToggleLanguage = {
                        val nextLang = if (currentLanguage == "en") "hi" else "en"
                        container.sessionManager.setLanguage(nextLang)
                    },
                    unreadNotificationsCount = unreadNotificationsCount,
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = CleankrCardSurface
                ) {
                    val items = listOf(
                        Triple(Screen.Home.route, CleankrStrings.get("home", currentLanguage), Icons.Default.Home),
                        Triple(Screen.Services.route, CleankrStrings.get("services", currentLanguage), Icons.Default.CleaningServices),
                        Triple(Screen.Bookings.route, CleankrStrings.get("bookings", currentLanguage), Icons.Default.ReceiptLong),
                        Triple(Screen.Profile.route, CleankrStrings.get("profile", currentLanguage), Icons.Default.Person)
                    )
                    items.forEach { (route, label, icon) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CleankrTeal,
                                selectedTextColor = CleankrTeal,
                                indicatorColor = CleankrCardSurface,
                                unselectedIconColor = CleankrNavy.copy(alpha = 0.6f),
                                unselectedTextColor = CleankrNavy.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userProfile = userProfile,
                    categories = container.serviceRepository.categories,
                    services = services,
                    activeBookings = activeBookings,
                    backendService = container.firebaseBackend,
                    onCategoryClick = { catId ->
                        navController.navigate(Screen.Services.route)
                    },
                    onServiceClick = { srvId ->
                        navController.navigate(Screen.ServiceDetail.createRoute(srvId))
                    },
                    onBookingClick = { bId ->
                        navController.navigate(Screen.BookingTracking.createRoute(bId))
                    },
                    onViewAllServices = {
                        navController.navigate(Screen.Services.route)
                    },
                    onNavigateToAdminHub = {
                        navController.navigate(Screen.AdminHub.route)
                    },
                    unresolvedAlertsCount = unresolvedAlertsCount
                )
            }

            composable(Screen.Services.route) {
                ServiceCatalogScreen(
                    categories = container.serviceRepository.categories,
                    services = services,
                    selectedCategoryId = "cat_all",
                    onSelectCategory = {},
                    onServiceClick = { srvId ->
                        navController.navigate(Screen.ServiceDetail.createRoute(srvId))
                    }
                )
            }

            composable(Screen.Bookings.route) {
                BookingHistoryScreen(
                    bookings = bookings,
                    bookingRepository = container.bookingRepository,
                    onBookingClick = { bId ->
                        navController.navigate(Screen.BookingTracking.createRoute(bId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    userProfile = userProfile,
                    sessionManager = container.sessionManager,
                    onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                    onNavigateToLegal = { title, content ->
                        navController.navigate(Screen.Legal.createRoute(title, android.net.Uri.encode(content)))
                    },
                    onNavigateToAdminHub = {
                        navController.navigate(Screen.AdminHub.route)
                    },
                    unresolvedAlertsCount = unresolvedAlertsCount,
                    onSignOut = onSignOut
                )
            }

            composable(
                route = Screen.ServiceDetail.route,
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val srvId = backStackEntry.arguments?.getString("serviceId") ?: ""
                val service = services.firstOrNull { it.id == srvId } ?: container.firebaseBackend.defaultServicesCatalog.first()
                ServiceDetailScreen(
                    service = service,
                    onBookNow = {
                        navController.navigate(Screen.BookingFlow.createRoute(service.id))
                    }
                )
            }

            composable(
                route = Screen.BookingFlow.route,
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val srvId = backStackEntry.arguments?.getString("serviceId") ?: ""
                val service = services.firstOrNull { it.id == srvId } ?: container.firebaseBackend.defaultServicesCatalog.first()
                BookingFlowScreen(
                    service = service,
                    savedAddresses = addresses,
                    bookingRepository = container.bookingRepository,
                    slotRepository = container.slotRepository,
                    firebaseBackend = container.firebaseBackend,
                    customerId = userProfile.uid,
                    adminHubRepository = container.adminHubRepository,
                    customerName = userProfile.name,
                    customerPhone = userProfile.phone,
                    onBookingSuccess = { bookingId ->
                        navController.navigate(Screen.BookingSuccess.createRoute(bookingId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onAddNewAddressClick = { navController.navigate(Screen.Addresses.route) }
                )
            }

            composable(
                route = Screen.BookingSuccess.route,
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val booking = bookings.firstOrNull { it.id == bookingId }
                    ?: Booking(id = bookingId, serviceTitle = "Cleankr Cleaning", startPin = "4819")
                BookingSuccessScreen(
                    booking = booking,
                    onTrackBooking = {
                        navController.navigate(Screen.BookingTracking.createRoute(bookingId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.BookingTracking.route,
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val booking = bookings.firstOrNull { it.id == bookingId }
                    ?: Booking(id = bookingId, serviceTitle = "Cleankr Hygiene Clean", startPin = "4819")
                BookingTrackingScreen(
                    booking = booking,
                    bookingRepository = container.bookingRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Addresses.route) {
                AddressManagementScreen(
                    addresses = addresses,
                    addressRepository = container.addressRepository,
                    backendService = container.firebaseBackend
                )
            }

            composable(Screen.Support.route) {
                HelpSupportScreen(
                    supportRepository = container.supportRepository,
                    tickets = supportTickets
                )
            }

            composable(Screen.Notifications.route) {
                NotificationCenterScreen(
                    notifications = notifications,
                    notificationRepository = container.notificationRepository,
                    onNotificationClick = { bId ->
                        if (bId != null) {
                            navController.navigate(Screen.BookingTracking.createRoute(bId))
                        }
                    }
                )
            }

            composable(
                route = Screen.Legal.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("content") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "Legal"
                val encodedContent = backStackEntry.arguments?.getString("content") ?: ""
                val content = android.net.Uri.decode(encodedContent)
                LegalDocumentScreen(title = title, content = content)
            }

            composable(Screen.AdminHub.route) {
                AdminHubScreen(
                    adminHubRepository = container.adminHubRepository,
                    backendService = container.firebaseBackend,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
