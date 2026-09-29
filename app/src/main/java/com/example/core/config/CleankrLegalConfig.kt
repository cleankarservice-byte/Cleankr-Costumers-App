package com.example.core.config

object CleankrLegalConfig {
    const val APP_NAME = "Cleankr"
    const val SUPPORT_EMAIL = "cleankarservice@gmail.com"
    const val SUPPORT_PHONE = "+91 80000 12345"
    const val SUPPORT_WHATSAPP = "+918000012345"
    const val PRIVACY_POLICY_WEB_URL = "https://ais-pre-lfg7z5pjhexdtvlvndsgcp-302048635733.asia-southeast1.run.app/privacy-policy.html"
    const val TERMS_WEB_URL = "https://ais-pre-lfg7z5pjhexdtvlvndsgcp-302048635733.asia-southeast1.run.app/terms.html"

    const val TERMS_OF_SERVICE = """
Cleankr Customer Terms & Conditions
Last updated: September 2026

1. Acceptance of Agreement
By creating an account, selecting a service, and booking via the Cleankr app, you explicitly acknowledge, agree, and verify these Terms and Conditions.

2. Scope of Services & Pune Hubs Division
Cleankr provides professional deep cleaning services across Pune designated service divisions (Pune West, Pune East, Pune North/PCMC, Pune South-East, and Pune Central). Service delivery is subject to verified hub availability and serviceable pincodes.

3. Transparent Pricing & Zero Tax Policy
- All rates shown in the Cleankr catalog are fixed, standard, and transparent.
- No hidden platform fees or surprise taxes are added. Cleankr operates on a strict 0% Extra Tax / Zero Platform Fee policy.
- Customers pay only for the selected base service configuration and requested add-ons.

4. Doorstep Safety & 4-Digit PIN Verification
- For customer safety and authentication, every booking generates a unique 4-digit Doorstep Safety PIN.
- Cleaning partners cannot begin or mark the job started without receiving and verifying this PIN directly from the customer at the doorstep.
- Do not share the PIN until the service partner arrives in person at your premises.

5. Payment Methods
- Cash on Delivery (COD) / Doorstep UPI: Pay after satisfactory inspection upon service completion.
- Online Payment: Instant UPI, Credit/Debit card, or Net Banking.

6. Cancellation & Rescheduling
- Free cancellation and slot rescheduling are permitted up to 2 hours prior to the scheduled appointment window.
- In case of partner delays exceeding 30 minutes, customers are entitled to priority re-allocation or hassle-free cancellation.

7. Customer Satisfaction & Quality Guarantee
- If any area included in the booked scope is unsatisfactory, customers must report within 24 hours through the in-app Help & Support channel for a free touch-up revisit.
- Cleankr partners use industry-grade, non-corrosive chemicals and high-grade microfiber tools to protect surfaces.

8. Contact & Redressal
For grievances or inquiries, contact Cleankr customer care:
Email: cleankarservice@gmail.com | Phone: +91 80000 12345
"""

    const val PRIVACY_POLICY = """
Cleankr Privacy Policy
Effective & Last updated: September 2026

Cleankr ("we", "our", or "us") is dedicated to safeguarding customer privacy and securing personal information for all users of the Cleankr mobile application.

1. Information We Collect
- Contact Details: Customer full name, phone number, and optional email address.
- Service Addresses: House/flat number, apartment name, street, landmark, city (Pune), and postal pincode for cleaning service delivery.
- Precise & Approximate Location: Accessed only with your foreground consent when using "Detect Location" to find your cleaning address. Cleankr does NOT track location in the background.
- Booking History: Records of services requested, timestamps, selected add-ons, doorstep 4-digit PIN, and ratings/reviews.
- Device & Notifications: Firebase Cloud Messaging (FCM) tokens to deliver transactional status updates, partner arrival notices, and security PIN verification.

2. Purpose and Usage of Data
- To allocate and dispatch verified cleaning professionals from the nearest Pune hub.
- To send transactional SMS, push notifications, and doorstep PIN updates.
- To generate authentic billing receipts and customer care records.

3. Zero Third-Party Advertising / Data Sharing
Cleankr does NOT sell, rent, trade, or share your phone numbers or personal records with third-party advertisers or telemarketers. Information is only shared with the assigned cleaning partner to perform the booked service.

4. Data Security & Storage
All communication between the mobile app and our Firebase backend (Project: cleankr-724ce) is encrypted over TLS/HTTPS. Sensitive verification PINs and authentication states are protected with strict Firestore security rules.

5. Data Retention & Account Deletion Policy
In compliance with Google Play Store policies, any user can request permanent deletion of their account and all associated personal data at any time:
- Email cleankarservice@gmail.com with subject "Delete My Account" stating your registered phone number.
- Or request deletion via Help & Support in the app.
- All associated records will be permanently removed within 48 hours.

6. User Rights & Account Control
You maintain the right to view, update, or remove your saved addresses and profile details at any time from the Profile tab in the application.

7. Contact Data Protection Officer
For any privacy concerns, email cleankarservice@gmail.com or call +91 80000 12345.
"""

    const val CANCELLATION_POLICY = """
Cleankr Cancellation & Refund Policy
Last updated: September 2026

1. Flexible 2-Hour Cancellation:
Cancel or reschedule your cleaning booking for free up to 2 hours before the scheduled time slot.

2. Doorstep PIN Guarantee:
Jobs can only be started when you verify the 4-digit PIN at the doorstep. If a partner cannot arrive, the booking is automatically eligible for instant rescheduling or cancellation without penalty.

3. 24-Hour Quality Revisit:
If our cleaning does not meet your expectations, notify our support team within 24 hours for a complimentary inspection and touch-up visit.
"""
}
