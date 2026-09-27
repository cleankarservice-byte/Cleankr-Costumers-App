package com.example.core.repository

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.AddOnItem
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ServiceRepository(
    private val firebaseBackend: FirebaseBackendService? = null
) {

    // Seed data reflecting official Cleankr fixed pricing architecture
    // Synchronizes dynamically with Firebase Firestore 'services' collection
    private val _services = MutableStateFlow(createInitialCatalog())
    val services: Flow<List<ServiceItem>> = _services.asStateFlow()

    fun startRealtimeSync(scope: CoroutineScope) {
        val fb = firebaseBackend ?: return
        if (!fb.isFirebaseConfigured()) return
        scope.launch {
            try {
                fb.observeServicesCatalog().collect { backendServices ->
                    if (backendServices.isNotEmpty()) {
                        updateCatalogFromBackend(backendServices)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback maintains official catalog
            }
        }
    }

    fun updateCatalogFromBackend(items: List<ServiceItem>) {
        if (items.isNotEmpty()) {
            _services.value = items
        }
    }

    fun getAllServices(): List<ServiceItem> = _services.value

    fun getServicesByCategory(category: ServiceCategory): Flow<List<ServiceItem>> {
        return services.map { list -> list.filter { it.category == category } }
    }

    fun getServiceById(id: String): ServiceItem? {
        return _services.value.firstOrNull { it.id == id }
    }

    fun searchServices(query: String): List<ServiceItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return _services.value
        return _services.value.filter {
            it.title.lowercase().contains(q) ||
            it.shortDesc.lowercase().contains(q) ||
            it.category.displayName.lowercase().contains(q)
        }
    }

    fun calculateTotal(service: ServiceItem, variant: ServiceVariant, quantity: Int, selectedAddOns: List<AddOnItem>): Int {
        val baseVariantTotal = variant.price * quantity.coerceAtLeast(1)
        val addOnsSum = selectedAddOns.sumOf { it.price }
        return baseVariantTotal + addOnsSum
    }

    private fun createInitialCatalog(): List<ServiceItem> {
        // Official Add-ons
        val glassPartitionAddOn = AddOnItem(
            id = "addon_glass_partition",
            name = "Glass Partition Add-on",
            price = 200,
            description = "Specialized acid-free treatment to restore frosted and stained shower glass partitions to showroom shine."
        )

        val chimneyAddOn = AddOnItem(
            id = "addon_kitchen_chimney",
            name = "Chimney",
            price = 200,
            description = "Deep degreasing of chimney mesh filters and baffle plates."
        )

        val cabinetsAddOn = AddOnItem(
            id = "addon_kitchen_cabinets",
            name = "Cabinets",
            price = 250,
            description = "Interior and exterior cabinet sanitization, shelf wiping, and dust removal."
        )

        val trolleysAddOn = AddOnItem(
            id = "addon_kitchen_trolleys",
            name = "Trolleys",
            price = 350,
            description = "Trolley baskets dismantling, track wash, and oil residue removal."
        )

        return listOf(
            // =================================================================
            // 1. BATHROOM CATEGORY
            // =================================================================
            ServiceItem(
                id = "srv_bath_intense",
                category = ServiceCategory.BATHROOM,
                title = "Bathroom Intense Clean",
                shortDesc = "Deep scrubbing of tiles, commode, washbasin, chrome fittings & mirrors",
                fullDesc = "Cleankr's signature high-pressure deep scrub that restores hygiene and shine. We deploy commercial single-disc floor scrubbers and non-corrosive chemical solutions to tackle grime, soap scum, and yellow stains safely.",
                rating = 4.88f,
                reviewsCount = 4280,
                durationText = "60 - 90 mins",
                basePrice = 450,
                variants = listOf(
                    ServiceVariant("var_bath_intense_1", "1 Bathroom", 450, "60 mins"),
                    ServiceVariant("var_bath_intense_2", "2 Bathrooms", 850, "110 mins"),
                    ServiceVariant("var_bath_intense_3", "3 Bathrooms", 1250, "160 mins")
                ),
                addOns = listOf(glassPartitionAddOn),
                included = listOf(
                    "Deep scrubbing of floor and wall tiles (up to 7 ft)",
                    "Sanitization of toilet commode, seat and jet spray",
                    "Mirror polishing with streak-free alcohol formulation",
                    "Washbasin and countertop descaling",
                    "Chrome fittings, faucets and taps buffing",
                    "Trash removal and dry wiping of door & switchboards"
                ),
                notIncluded = listOf(
                    "Hard water thick scale peeling on broken surfaces",
                    "Ceiling repaint or wall leakage repair",
                    "Washing personal laundry or bath accessories"
                ),
                importantNotes = listOf(
                    "Continuous electricity and running water are required during service.",
                    "Please keep delicate personal toiletries inside the cabinet."
                )
            ),

            ServiceItem(
                id = "srv_bath_movein",
                category = ServiceCategory.BATHROOM,
                title = "Bathroom Move-in Clean",
                shortDesc = "Intensive disinfection, corner-to-corner sanitization for tenant shift",
                fullDesc = "Designed for tenants moving into a new flat or landlords preparing for inspection. Cleans previous tenant marks, grout discoloration, tile cement smears, and deep cabinet interiors.",
                rating = 4.92f,
                reviewsCount = 1890,
                durationText = "75 - 120 mins",
                basePrice = 550,
                variants = listOf(
                    ServiceVariant("var_bath_movein_1", "1 Bathroom", 550, "75 mins"),
                    ServiceVariant("var_bath_movein_2", "2 Bathrooms", 950, "130 mins"),
                    ServiceVariant("var_bath_movein_3", "3 Bathrooms", 1350, "180 mins")
                ),
                addOns = listOf(glassPartitionAddOn),
                included = listOf(
                    "Full wall tile ceiling-to-floor mechanical buffing",
                    "Complete hospital-grade sanitization and germ guard",
                    "Inside & outside cleaning of medicine cabinets & shelves",
                    "Geyser exterior and exhaust pipe dust extraction",
                    "Under-sink plumbing grease and dust removal",
                    "Anti-bacterial floor seal application"
                ),
                notIncluded = listOf(
                    "Tile grout re-grouting or silicone re-caulking",
                    "Removal of broken mirror glass"
                ),
                importantNotes = listOf(
                    "Ensure previous tenant's belongings are cleared before service."
                )
            ),

            ServiceItem(
                id = "srv_bath_hardwater",
                category = ServiceCategory.BATHROOM,
                title = "Bathroom Hard Water Removal",
                shortDesc = "Heavy chemical descaling for stubborn white calcium & mineral deposits",
                fullDesc = "Targeted treatment for homes with severe borewell or hard municipal water. Our German calcium-dissolving compounds dissolve stubborn white limescale from taps, shower roses, floor borders, and tiles without dulling glaze.",
                rating = 4.95f,
                reviewsCount = 3120,
                durationText = "90 - 150 mins",
                basePrice = 700,
                variants = listOf(
                    ServiceVariant("var_bath_hardwater_1", "1 Bathroom", 700, "90 mins"),
                    ServiceVariant("var_bath_hardwater_2", "2 Bathrooms", 1100, "150 mins"),
                    ServiceVariant("var_bath_hardwater_3", "3 Bathrooms", 1600, "210 mins")
                ),
                addOns = listOf(glassPartitionAddOn),
                included = listOf(
                    "Concentrated mineral scale dissolution on bathroom fixtures",
                    "Shower head unclogging & descaling treatment",
                    "Wall and floor grout calcium line extraction",
                    "Commode internal rim heavy scale treatment",
                    "Hydrophobic water-repellent coating on chrome taps"
                ),
                notIncluded = listOf(
                    "Repair of pre-existing metal chrome peeling or rust erosion"
                ),
                importantNotes = listOf(
                    "Takes longer than regular clean due to required chemical reaction time."
                )
            ),

            // =================================================================
            // 2. KITCHEN CATEGORY
            // =================================================================
            ServiceItem(
                id = "srv_kitchen_clean",
                category = ServiceCategory.KITCHEN,
                title = "Kitchen Cleaning",
                shortDesc = "Stove degreasing, backsplash scrub, counter slab, sink & appliance exterior polish",
                fullDesc = "Complete kitchen deep cleaning cutting through grease, oil stains, spices, and cooking grime. Restores hygiene and sparkle to your cooking space.",
                rating = 4.89f,
                reviewsCount = 2840,
                durationText = "120 mins",
                basePrice = 1400,
                variants = listOf(
                    ServiceVariant("var_kitchen_standard", "Kitchen Cleaning", 1400, "120 mins")
                ),
                addOns = listOf(chimneyAddOn, cabinetsAddOn, trolleysAddOn),
                included = listOf(
                    "Gas stove burners, knobs, and drip tray degreasing",
                    "Countertop and backsplash tile deep scrub",
                    "Stainless steel sink descaling and faucet buffing",
                    "Exterior wipedown of appliances (fridge, microwave, oven)",
                    "Floor machine scrub and trash clearance"
                ),
                notIncluded = listOf(
                    "Dismantling internal chimney motor coil",
                    "Washing personal crockery or dishes"
                ),
                importantNotes = listOf(
                    "Keep raw vegetables and unpacked food items safely covered."
                )
            ),

            // =================================================================
            // 3. FLAT CATEGORY
            // =================================================================
            ServiceItem(
                id = "srv_flat_deep",
                category = ServiceCategory.FLAT,
                title = "Full Home Deep Cleaning",
                shortDesc = "Complete 360° deep clean of rooms, floors, windows & cobweb removal",
                fullDesc = "Comprehensive whole-apartment sanitization including living room, bedrooms, balcony, dry balcony, windows, tracks, ceiling fans, and single-disc machine floor buffing.",
                rating = 4.93f,
                reviewsCount = 5420,
                durationText = "3 - 6 hours",
                basePrice = 3000,
                variants = listOf(
                    ServiceVariant("var_flat_1bhk", "1 BHK", 3000, "180 mins"),
                    ServiceVariant("var_flat_2bhk", "2 BHK", 5000, "240 mins"),
                    ServiceVariant("var_flat_3bhk", "3 BHK", 7000, "300 mins"),
                    ServiceVariant("var_flat_4bhk", "4 BHK", 9200, "360 mins")
                ),
                addOns = listOf(glassPartitionAddOn, chimneyAddOn),
                included = listOf(
                    "Ceiling cobweb dusting and fan blade wipe",
                    "Window glass, grill and slider track vacuuming",
                    "All room floor scrubbing with rotary buffer machine",
                    "Balcony pressure wash and railing wipe",
                    "Doors, switchboards, and baseboard cleaning"
                ),
                notIncluded = listOf(
                    "Wall painting or chemical paint spot removal",
                    "Moving heavy solid teak furniture older than 10 years"
                ),
                importantNotes = listOf(
                    "Team of 2-4 trained Cleankr pros will be dispatched."
                )
            ),

            // =================================================================
            // 4. BALCONY CATEGORY (OTHER / BALCONY)
            // =================================================================
            ServiceItem(
                id = "srv_balcony_clean",
                category = ServiceCategory.BALCONY,
                title = "Balcony Cleaning",
                shortDesc = "Pigeon drop removal, floor jet wash, railing and bird net dusting",
                fullDesc = "Specially formulated for open urban balconies subjected to pollution, pigeon droppings, moss, and weather grime. High-pressure jet cleaning restores tiles and safety grills.",
                rating = 4.84f,
                reviewsCount = 1580,
                durationText = "45 - 75 mins",
                basePrice = 600,
                variants = listOf(
                    ServiceVariant("var_balc_small", "Small Balcony", 600, "45 mins"),
                    ServiceVariant("var_balc_big", "Big Balcony", 850, "75 mins")
                ),
                addOns = emptyList(),
                included = listOf(
                    "Pigeon dropping softening and bio-hazard sanitized wipe",
                    "Safety railing and bird net dusting",
                    "Floor scrubbing and moss removal",
                    "Drain trap clean and odor flush"
                ),
                notIncluded = listOf(
                    "Pigeon net installation or wire replacement",
                    "Cleaning outside of parapet wall overlooking height"
                ),
                importantNotes = listOf(
                    "Ensure drain outlet is unclogged before washing."
                )
            ),

            // =================================================================
            // 5. OTHER CLEANING CATEGORY
            // =================================================================
            ServiceItem(
                id = "srv_other_fan",
                category = ServiceCategory.OTHER,
                title = "Fan Cleaning",
                shortDesc = "Ceiling fan blades, motor housing dust extraction & wiping",
                fullDesc = "Professional high-reach vacuuming, dry wiping, and chemical degreasing of ceiling fan blades and motor tops to remove caked dust and grime.",
                rating = 4.86f,
                reviewsCount = 890,
                durationText = "15 - 20 mins",
                basePrice = 60,
                variants = listOf(
                    ServiceVariant("var_fan_clean", "1 Ceiling Fan", 60, "15 mins")
                ),
                addOns = emptyList(),
                included = listOf(
                    "Blades top & bottom deep wiping",
                    "Motor casing dust removal",
                    "Drop cloth placed to catch loose dust"
                ),
                notIncluded = listOf(
                    "Electrical motor rewiring or regulator repairs"
                ),
                importantNotes = listOf(
                    "Fans must be switched off prior to partner arrival."
                )
            ),

            ServiceItem(
                id = "srv_other_exhaust_fan",
                category = ServiceCategory.OTHER,
                title = "Exhaust Fan Cleaning",
                shortDesc = "Complete mesh, grill and greasy blade degreasing",
                fullDesc = "Intensive degreasing and cleaning of kitchen or bathroom exhaust fan blades and louvers to restore maximum airflow.",
                rating = 4.88f,
                reviewsCount = 760,
                durationText = "20 - 30 mins",
                basePrice = 65,
                variants = listOf(
                    ServiceVariant("var_exhaust_clean", "1 Exhaust Fan", 65, "20 mins")
                ),
                addOns = emptyList(),
                included = listOf(
                    "Blade degreasing and hot water chemical rinse",
                    "Outer mesh grill scrubbing",
                    "Surrounding wall wipe"
                ),
                notIncluded = listOf(
                    "Exhaust duct chimney masonry work"
                ),
                importantNotes = listOf(
                    "Exhaust unit must have accessible power switch."
                )
            ),

            ServiceItem(
                id = "srv_other_glass_window",
                category = ServiceCategory.OTHER,
                title = "Glass Window Cleaning",
                shortDesc = "Streak-free window panes, frames, and aluminum slider track cleaning",
                fullDesc = "Crystal-clear squeegee cleaning of glass window panes, window frames, and vacuuming of slider tracks.",
                rating = 4.87f,
                reviewsCount = 1120,
                durationText = "30 - 45 mins",
                basePrice = 300,
                variants = listOf(
                    ServiceVariant("var_window_clean", "Glass Window", 300, "30 mins")
                ),
                addOns = emptyList(),
                included = listOf(
                    "Streak-free glass pane polish",
                    "Frame wipe and corner vacuum",
                    "Slider track grime removal"
                ),
                notIncluded = listOf(
                    "Exterior glass on high-rise buildings without safety balcony"
                ),
                importantNotes = listOf(
                    "Window sliders must be structurally sound."
                )
            ),

            ServiceItem(
                id = "srv_other_glass_door",
                category = ServiceCategory.OTHER,
                title = "Glass Door Cleaning",
                shortDesc = "Streak-free polishing of large glass doors and French doors",
                fullDesc = "Specialized ammonia-free descaling and squeegee buffing for large sliding glass doors, balcony doors, and interior glass partitions.",
                rating = 4.90f,
                reviewsCount = 940,
                durationText = "40 - 55 mins",
                basePrice = 400,
                variants = listOf(
                    ServiceVariant("var_door_clean", "Glass Door", 400, "40 mins")
                ),
                addOns = emptyList(),
                included = listOf(
                    "Full glass panel streak-free polish on both sides",
                    "Handle and chrome frame buffing",
                    "Bottom runner track cleaning"
                ),
                notIncluded = listOf(
                    "Cracked glass repair or tint film removal"
                ),
                importantNotes = listOf(
                    "Please notify pros of any pre-existing glass scratches."
                )
            )
        )
    }
}
