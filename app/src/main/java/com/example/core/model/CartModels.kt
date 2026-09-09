package com.example.core.model

/** Immutable product configuration chosen by the customer before adding a product to the cart. */
data class CartAddon(
    val name: String,
    val price: Double = 0.0
)

data class CartSelection(
    val selectedSize: String = "",
    val sizePriceAdjustment: Double = 0.0,
    val selectedAddons: List<CartAddon> = emptyList(),
    val specialNotes: String = ""
) {
    val additionalPrice: Double
        get() = sizePriceAdjustment + selectedAddons.sumOf { it.price }

    val optionsSummary: String
        get() = buildList {
            if (selectedSize.isNotBlank()) add("الحجم: $selectedSize")
            if (selectedAddons.isNotEmpty()) {
                add("الإضافات: " + selectedAddons.joinToString("، ") { addon ->
                    if (addon.price > 0) "${addon.name} (+${addon.price.toInt()} ج.م)" else addon.name
                })
            }
            if (specialNotes.isNotBlank()) add("ملاحظة: ${specialNotes.trim()}")
        }.joinToString(" • ")
}

/**
 * A cart line is identified by the product plus its exact configuration.
 * Therefore the same product with different add-ons is kept as a separate line.
 */
data class CartItem(
    val productId: Long,
    val productName: String,
    val imageEmoji: String,
    val baseUnitPrice: Double,
    val quantity: Int,
    val selection: CartSelection = CartSelection()
) {
    val key: String
        get() {
            val addonsKey = selection.selectedAddons
                .sortedWith(compareBy<CartAddon> { it.name }.thenBy { it.price })
                .joinToString("|") { "${it.name}:${it.price}" }
            return listOf(
                productId.toString(),
                selection.selectedSize.trim(),
                addonsKey,
                selection.specialNotes.trim()
            ).joinToString("::")
        }

    val unitPrice: Double
        get() = baseUnitPrice + selection.additionalPrice

    val totalPrice: Double
        get() = unitPrice * quantity

    val optionsSummary: String
        get() = selection.optionsSummary
}
