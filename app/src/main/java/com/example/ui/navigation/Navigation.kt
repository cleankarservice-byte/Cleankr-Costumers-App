package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Services : Screen("services")
    data object Bookings : Screen("bookings")
    data object Profile : Screen("profile")

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

    data object Addresses : Screen("addresses")
    data object Support : Screen("support")
    data object Notifications : Screen("notifications")
    data object AdminHub : Screen("admin_hub")
    data object Legal : Screen("legal/{title}/{content}") {
        fun createRoute(title: String, content: String) = "legal/$title/$content"
    }
}
