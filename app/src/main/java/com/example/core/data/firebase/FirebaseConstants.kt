package com.example.core.data.firebase

/**
 * Standard Firebase Firestore collection names and field constants
 * shared across the Cleankr Ecosystem (Customer App, Partner App, Admin Panel).
 */
object FirebaseConstants {
    // Official Firebase Target Configuration
    const val PROJECT_ID = "cleankr-724ce"
    const val STORAGE_BUCKET = "cleankr-724ce.firebasestorage.app"

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CUSTOMERS = "customers"
    const val COLLECTION_SERVICES = "services"
    const val COLLECTION_SERVICE_CATEGORIES = "service_categories"
    const val COLLECTION_BOOKINGS = "bookings"
    const val COLLECTION_BOOKING_STATUS = "booking_status"
    const val COLLECTION_ADDRESSES = "addresses"
    const val COLLECTION_PAYMENTS = "payments"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_SUPPORT_TICKETS = "support_tickets"
    const val COLLECTION_REVIEWS = "reviews"

    // Subcollections
    const val SUBCOLLECTION_ADDRESSES = "addresses"

    // Ecosystem Role Constants
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_PARTNER = "partner"
    const val ROLE_ADMIN = "admin"

    // Universal Field Names (compatible with Partner App & Admin Panel)
    const val FIELD_BOOKING_ID = "bookingId"
    const val FIELD_CUSTOMER_ID = "customerId"
    const val FIELD_PARTNER_ID = "partnerId"
    const val FIELD_SERVICE_ID = "serviceId"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_UPDATED_AT = "updatedAt"
    const val FIELD_STATUS = "status"
    const val FIELD_ROLE = "role"
    const val FIELD_FCM_TOKEN = "fcmToken"
    const val FIELD_FCM_TOKENS = "fcmTokens"

    // Partner Status Constants
    const val STATUS_BOOKED = "BOOKED"
    const val STATUS_ASSIGNED = "ASSIGNED"
    const val STATUS_PARTNER_ACCEPTED = "PARTNER_ACCEPTED"
    const val STATUS_ON_THE_WAY = "ON_THE_WAY"
    const val STATUS_ARRIVED = "ARRIVED"
    const val STATUS_STARTED = "STARTED"
    const val STATUS_COMPLETED = "COMPLETED"
    const val STATUS_CANCELLED = "CANCELLED"
}
