package com.example.ui.components

object CleankrStrings {
    private val en = mapOf(
        "app_title" to "Cleankr",
        "tagline" to "Professional Home & Deep Cleaning Services",
        "home" to "Home",
        "services" to "Services",
        "bookings" to "My Bookings",
        "profile" to "Profile",
        "book_now" to "Book Service",
        "select_variant" to "Select Variant & Options",
        "add_ons" to "Recommended Add-ons",
        "select_slot" to "Select Date & Time Slot",
        "select_address" to "Select Service Address",
        "doorstep_otp" to "Doorstep Safety PIN",
        "doorstep_otp_desc" to "Share this 4-digit code with your professional cleaner upon arrival to start work safely.",
        "reschedule" to "Reschedule Booking",
        "cancel_booking" to "Cancel Booking",
        "download_invoice" to "Share Bill / Receipt",
        "refer_earn" to "Refer & Earn ₹100",
        "refer_desc" to "Share your invite code with friends & neighbors. They get ₹100 off, and you earn ₹100 upon service completion!",
        "rate_service" to "Rate Your Clean",
        "review_prompt" to "How was your cleaning experience?",
        "use_location" to "Use Current Location (GPS)",
        "switch_lang" to "हिंदी में बदलें",
        "hub_assigned" to "Assigned Hub",
        "verified_pro" to "100% Background Verified Partner",
        "cod" to "Pay After Service (Cash / UPI at Doorstep)",
        "online_pay" to "Pay Online (UPI / Card)",
        "share_on_whatsapp" to "Share on WhatsApp"
    )

    private val hi = mapOf(
        "app_title" to "क्लीनकर (Cleankr)",
        "tagline" to "विश्वसनीय और प्रोफेशनल डीप क्लीनिंग सेवाएं",
        "home" to "होम",
        "services" to "सेवाएं",
        "bookings" to "मेरी बुकिंग",
        "profile" to "प्रोफाइल",
        "book_now" to "अभी बुक करें",
        "select_variant" to "वेरिएंट और विकल्प चुनें",
        "add_ons" to "अतिरिक्त ऐड-ऑन्स",
        "select_slot" to "तारीख और समय चुनें",
        "select_address" to "सफाई का पता चुनें",
        "doorstep_otp" to "डोरस्टेप सुरक्षा पिन (OTP)",
        "doorstep_otp_desc" to "काम शुरू करने से पहले अपने क्लीनर को यह 4-अंकीय पिन बताएं। यह आपकी सुरक्षा सुनिश्चित करता है।",
        "reschedule" to "समय/तारीख बदलें (Reschedule)",
        "cancel_booking" to "बुकिंग रद्द करें",
        "download_invoice" to "बिल / रसीद शेयर करें",
        "refer_earn" to "रेफ़र करें और ₹100 पाएं",
        "refer_desc" to "अपने दोस्तों और पड़ोसियों को कोड भेजें। उन्हें ₹100 की छूट मिलेगी और आपको ₹100 मिलेंगे!",
        "rate_service" to "सफाई की रेटिंग दें",
        "review_prompt" to "आपकी सफाई सेवा का अनुभव कैसा रहा?",
        "use_location" to "वर्तमान लोकेशन (GPS) का उपयोग करें",
        "switch_lang" to "Switch to English",
        "hub_assigned" to "संबद्ध हब",
        "verified_pro" to "100% पुलिस वेरिफाइड पार्टनर",
        "cod" to "काम के बाद भुगतान (Cash / UPI)",
        "online_pay" to "ऑनलाइन भुगतान (UPI / कार्ड)",
        "share_on_whatsapp" to "व्हाट्सएप पर शेयर करें"
    )

    fun get(key: String, lang: String): String {
        return if (lang == "hi") {
            hi[key] ?: en[key] ?: key
        } else {
            en[key] ?: key
        }
    }
}
