package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.di.AppContainer
import com.example.core.model.ServiceCategory
import com.example.ui.MainAppScreen
import com.example.ui.address.AddressManagementScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.OtpVerificationScreen
import com.example.ui.auth.PinLoginScreen
import com.example.ui.auth.PinSetupScreen
import com.example.ui.auth.SplashScreen
import com.example.ui.booking.BookingFlowScreen
import com.example.ui.booking.BookingSuccessScreen
import com.example.ui.navigation.Screen
import com.example.ui.profile.AccountDeletionScreen
import com.example.ui.profile.PoliciesScreen
import com.example.ui.services.ServiceDetailScreen
import com.example.ui.services.ServiceListScreen
import com.example.ui.support.HelpSupportScreen
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tracking.BookingTrackingScreen

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CleankrBackground
                ) {
                    CleankrCustomerAppNavHost(appContainer = appContainer)
                }
            }
        }
    }
}

@Composable
fun CleankrCustomerAppNavHost(appContainer: AppContainer) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(appContainer.sessionManager, appContainer.firebaseBackend)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Destination
        composable(Screen.Splash.route) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPinLogin = {
                    navController.navigate(Screen.PinLogin.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Mobile Login
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onOtpSent = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                },
                onNavigateToPinLogin = {
                    navController.navigate(Screen.PinLogin.route)
                }
            )
        }

        // OTP Verification
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpVerificationScreen(
                phone = phone,
                authViewModel = authViewModel,
                onSuccess = { needsPinSetup ->
                    if (needsPinSetup) {
                        navController.navigate(Screen.PinSetup.createRoute(phone)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.MainApp.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Quick PIN Setup
        composable(
            route = Screen.PinSetup.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) {
            PinSetupScreen(
                authViewModel = authViewModel,
                onPinSetSuccess = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // PIN Login
        composable(Screen.PinLogin.route) {
            PinLoginScreen(
                authViewModel = authViewModel,
                onSuccess = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.PinLogin.route) { inclusive = true }
                    }
                },
                onFallbackToOtp = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PinLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Dashboard (Home, Bookings, Alerts, Profile)
        composable(Screen.MainApp.route) {
            MainAppScreen(
                appContainer = appContainer,
                onNavigateToCategory = { category ->
                    navController.navigate(Screen.ServiceList.createRoute(category.name))
                },
                onNavigateToService = { serviceId ->
                    navController.navigate(Screen.ServiceDetail.createRoute(serviceId))
                },
                onNavigateToBooking = { bookingId ->
                    navController.navigate(Screen.BookingTracking.createRoute(bookingId))
                },
                onNavigateToAddresses = {
                    navController.navigate(Screen.AddressManagement.route)
                },
                onNavigateToSupport = {
                    navController.navigate(Screen.HelpSupport.route)
                },
                onNavigateToAccountDeletion = {
                    navController.navigate(Screen.AccountDeletion.route)
                },
                onNavigateToPolicies = { type ->
                    navController.navigate(Screen.Policies.createRoute(type))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Service Catalog Category
        composable(
            route = Screen.ServiceList.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val catName = backStackEntry.arguments?.getString("category") ?: ServiceCategory.BATHROOM.name
            val category = try { ServiceCategory.valueOf(catName) } catch (_: Exception) { ServiceCategory.BATHROOM }

            ServiceListScreen(
                category = category,
                serviceRepository = appContainer.serviceRepository,
                onServiceClick = { serviceId ->
                    navController.navigate(Screen.ServiceDetail.createRoute(serviceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Service Details
        composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            ServiceDetailScreen(
                serviceId = serviceId,
                serviceRepository = appContainer.serviceRepository,
                onBookNow = { id ->
                    navController.navigate(Screen.BookingFlow.createRoute(id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Multi-step Booking Flow
        composable(
            route = Screen.BookingFlow.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            BookingFlowScreen(
                serviceId = serviceId,
                serviceRepository = appContainer.serviceRepository,
                slotRepository = appContainer.slotRepository,
                addressRepository = appContainer.addressRepository,
                bookingRepository = appContainer.bookingRepository,
                onBookingCompleted = { bookingId ->
                    navController.navigate(Screen.BookingSuccess.createRoute(bookingId)) {
                        popUpTo(Screen.BookingFlow.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Booking Success
        composable(
            route = Screen.BookingSuccess.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingSuccessScreen(
                bookingId = bookingId,
                bookingRepository = appContainer.bookingRepository,
                onTrackBooking = { id ->
                    navController.navigate(Screen.BookingTracking.createRoute(id)) {
                        popUpTo(Screen.MainApp.route)
                    }
                },
                onGoToHome = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.MainApp.route) { inclusive = true }
                    }
                }
            )
        }

        // Live Tracking & Details
        composable(
            route = Screen.BookingTracking.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingTrackingScreen(
                bookingId = bookingId,
                bookingRepository = appContainer.bookingRepository,
                slotRepository = appContainer.slotRepository,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Saved Addresses
        composable(Screen.AddressManagement.route) {
            AddressManagementScreen(
                addressRepository = appContainer.addressRepository,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Help & Support
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                supportRepository = appContainer.supportRepository,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Account Deletion
        composable(Screen.AccountDeletion.route) {
            AccountDeletionScreen(
                authViewModel = authViewModel,
                onDeleted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Policies
        composable(
            route = Screen.Policies.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "cancellation"
            PoliciesScreen(
                policyType = type,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
