package com.example.core.repository

import com.example.core.model.AddOnItem
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ServiceRepository {

    // Seed data reflecting official Cleankr fixed pricing architecture
    // In production, this syncs directly from Firebase Firestore / Cleankr Backend API
    private val _services = MutableStateFlow(createInitialCatalog())
    val services: Flow<List<ServiceItem>> = _services.asStateFlow()

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
        val glassPartitionAddOn = AddOnItem(
            id = "addon_glass_partition",
            name = "Glass Partition Descaling & Polish",
            price = 200,
            description = "Specialized acid-free treatment to restore frosted and stained shower glass partitions to showroom shine."
        )

        val exhaustFanAddOn = AddOnItem(
            id = "addon_exhaust_fan",
            name = "Exhaust Fan Degreasing",
            price = 150,
            description = "Complete dismantling, deep degreasing of blades and outer mesh grill."
        )

        val drainDeodorizerAddOn = AddOnItem(
            id = "addon_drain_bio",
            name = "Bio-Enzymatic Drain Flusher",
            price = 120,
            description = "High-potency bio-enzymatic drain treatment preventing odor and sludge buildup."
        )

        val chimneyFilterAddOn = AddOnItem(
            id = "addon_chimney_filter",
            name = "Kitchen Chimney Baffle Filter Soak",
            price = 250,
            description = "Intense alkaline soak and steam blast to strip grease and burnt oils."
        )

        return listOf(
            // BATHROOM CATEGORY (Exact pricing specified in requirements)
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
                addOns = listOf(glassPartitionAddOn, exhaustFanAddOn, drainDeodorizerAddOn),
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
                addOns = listOf(glassPartitionAddOn, exhaustFanAddOn, drainDeodorizerAddOn),
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
                addOns = listOf(glassPartitionAddOn, exhaustFanAddOn),
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

            // KITCHEN CATEGORY
            ServiceItem(
                id = "srv_kitchen_deep",
                category = ServiceCategory.KITCHEN,
                title = "Kitchen Deep Degreasing & Sanitization",
                shortDesc = "Industrial stove degreasing, backsplash scrub, slab & sink polish",
                fullDesc = "Cuts through sticky Indian cooking oil vapours, spice residue, and blackened grease on gas stoves, tiles, countertops, and exterior cabinetry.",
                rating = 4.86f,
                reviewsCount = 2740,
                durationText = "90 - 140 mins",
                basePrice = 899,
                variants = listOf(
                    ServiceVariant("var_kitch_std", "Standard Kitchen (upto 80 sq.ft)", 899, "90 mins"),
                    ServiceVariant("var_kitch_large", "Large / Modular Kitchen (80+ sq.ft)", 1399, "140 mins")
                ),
                addOns = listOf(chimneyFilterAddOn, exhaustFanAddOn),
                included = listOf(
                    "Gas burners, knobs, and drip tray degreasing",
                    "Kitchen backsplash tile scrub and grease removal",
                    "Granite slab descaling and steel sink buffing",
                    "Exterior wipedown of modular cabinets and drawers",
                    "Fridge & microwave exterior wipedown"
                ),
                notIncluded = listOf(
                    "Emptying packed kitchen food containers or grocery jars",
                    "Chimney motor coil dismantling"
                ),
                importantNotes = listOf(
                    "Kitchen must be empty of raw vegetables and open food items."
                )
            ),

            // FLAT CATEGORY
            ServiceItem(
                id = "srv_flat_deep",
                category = ServiceCategory.FLAT,
                title = "Full Home Deep Cleaning",
                shortDesc = "Complete 360° deep clean of rooms, floors, windows & cobweb removal",
                fullDesc = "Comprehensive whole-apartment sanitization including living room, bedrooms, balcony, dry balcony, windows, tracks, ceiling fans, and single-disc machine floor buffing.",
                rating = 4.91f,
                reviewsCount = 5210,
                durationText = "3 - 5 hours",
                basePrice = 1899,
                variants = listOf(
                    ServiceVariant("var_flat_1bhk", "1 BHK Full Deep Clean", 1899, "180 mins"),
                    ServiceVariant("var_flat_2bhk", "2 BHK Full Deep Clean", 2799, "240 mins"),
                    ServiceVariant("var_flat_3bhk", "3 BHK Full Deep Clean", 3699, "300 mins")
                ),
                addOns = listOf(glassPartitionAddOn, chimneyFilterAddOn, exhaustFanAddOn),
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
                    "Team of 2-3 trained Cleankr pros will be dispatched."
                )
            ),

            // BALCONY CATEGORY
            ServiceItem(
                id = "srv_balcony_clean",
                category = ServiceCategory.BALCONY,
                title = "Balcony & Utility Area Clean",
                shortDesc = "Pigeon drop removal, floor jet wash, railing and bird net dusting",
                fullDesc = "Specially formulated for open urban balconies subjected to pollution, pigeon droppings, moss, and weather grime. High-pressure jet cleaning restores tiles and safety grills.",
                rating = 4.82f,
                reviewsCount = 1430,
                durationText = "45 - 75 mins",
                basePrice = 499,
                variants = listOf(
                    ServiceVariant("var_balc_1", "1 Standard Balcony", 499, "45 mins"),
                    ServiceVariant("var_balc_2", "2 Balconies", 849, "75 mins"),
                    ServiceVariant("var_balc_3", "3 Balconies / Large Terrace", 1249, "110 mins")
                ),
                addOns = listOf(drainDeodorizerAddOn),
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

            // OTHER CATEGORY
            ServiceItem(
                id = "srv_other_sofa",
                category = ServiceCategory.OTHER,
                title = "Sofa & Upholstery Shampooing",
                shortDesc = "Fabric foam extraction, dust-mite suction & anti-allergen treatment",
                fullDesc = "Deep industrial injection-extraction shampooing that extracts deeply embedded sweat, dust mites, pet hair, and drink spills from fabric and suede couches.",
                rating = 4.89f,
                reviewsCount = 2190,
                durationText = "60 - 90 mins",
                basePrice = 599,
                variants = listOf(
                    ServiceVariant("var_sofa_3seater", "3 Seater Sofa", 599, "60 mins"),
                    ServiceVariant("var_sofa_5seater", "5 Seater (3+1+1 or L-shape)", 899, "90 mins"),
                    ServiceVariant("var_sofa_7seater", "7 Seater Jumbo Sectional", 1299, "120 mins")
                ),
                addOns = listOf(
                    AddOnItem("addon_mattress", "Single Mattress Sanitization", 350, "UV-C light and steam suction of mattress.")
                ),
                included = listOf(
                    "High-suction dry vacuuming",
                    "Foam scrub with neutral pH fabric cleanser",
                    "Extraction of dirty moisture with heavy vacuum",
                    "Aroma deodorizer spray"
                ),
                notIncluded = listOf(
                    "Pure leather conditioning (different service)",
                    "Drying within 3 hours in rainy humid weather (requires fan/AC)"
                ),
                importantNotes = listOf(
                    "Takes 3 to 4 hours to dry under ceiling fan after cleaning."
                )
            ),

            ServiceItem(
                id = "srv_other_glass",
                category = ServiceCategory.OTHER,
                title = "Glass Windows & Partition Descaling",
                shortDesc = "Exterior and interior sliding glass squeegee polish and track clean",
                fullDesc = "Restore absolute crystal clarity to balcony sliding glass doors, French windows, and interior frosted partition glass with zero streaks.",
                rating = 4.87f,
                reviewsCount = 980,
                durationText = "45 - 60 mins",
                basePrice = 399,
                variants = listOf(
                    ServiceVariant("var_glass_upto3", "Upto 3 Glass Panels/Windows", 399, "45 mins"),
                    ServiceVariant("var_glass_upto6", "Upto 6 Glass Panels/Windows", 699, "70 mins")
                ),
                addOns = listOf(glassPartitionAddOn),
                included = listOf(
                    "Rubber squeegee streak-free cleaning",
                    "Aluminum slider track vacuuming & grime scraping",
                    "Handle and frame wipe"
                ),
                notIncluded = listOf(
                    "High-rise hanging rope exterior facade cleaning"
                ),
                importantNotes = listOf(
                    "Window sliders must be structurally intact."
                )
            )
        )
    }
}
