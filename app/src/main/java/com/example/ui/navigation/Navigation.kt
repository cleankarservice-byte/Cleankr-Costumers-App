package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object OtpVerification : Screen("otp_verification/{phone}") {
        fun createRoute(phone: String) = "otp_verification/$phone"
    }
    data object PinSetup : Screen("pin_setup/{phone}") {
        fun createRoute(phone: String) = "pin_setup/$phone"
    }
    data object PinLogin : Screen("pin_login")

    data object MainApp : Screen("main_app")

    data object ServiceList : Screen("service_list/{category}") {
        fun createRoute(category: String) = "service_list/$category"
    }
    data object ServiceDetail : Screen("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    data object BookingFlow : Screen("booking_flow/{serviceId}") {
        fun createRoute(serviceId: String) = "booking_flow/$serviceId"
    }
    data object BookingSuccess : Screen("booking_success/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_success/$bookingId"
    }
    data object BookingTracking : Screen("booking_tracking/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_tracking/$bookingId"
    }
    data object AddressManagement : Screen("address_management")
    data object HelpSupport : Screen("help_support")
    data object AccountDeletion : Screen("account_deletion")
    data object Policies : Screen("policies/{type}") {
        fun createRoute(type: String) = "policies/$type"
    }
}

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "bottom_tab_home"),
    BOOKINGS("Bookings", Icons.Filled.Assignment, Icons.Outlined.Assignment, "bottom_tab_bookings"),
    NOTIFICATIONS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, "bottom_tab_notifications"),
    ACCOUNT("Account", Icons.Filled.Person, Icons.Outlined.Person, "bottom_tab_account")
}
