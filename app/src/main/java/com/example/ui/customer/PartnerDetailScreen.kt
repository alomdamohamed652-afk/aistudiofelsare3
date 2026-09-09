package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*
import com.example.data.local.PartnerEntity
import com.example.data.local.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDetailScreen(
    partner: PartnerEntity,
    products: List<ProductEntity>,
    onBack: () -> Unit,
    onAddToCart: (ProductEntity, Int, String) -> Unit,
    onViewCart: () -> Unit,
    cartItemCount: Int,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var productForSheet by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = remember(products) {
        products.map { it.category }.distinct()
    }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == null) products
        else products.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = partner.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("partner_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = onViewCart, modifier = Modifier.testTag("partner_cart_icon")) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(containerColor = BrandPrimary) { Text("$cartItemCount") }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "السلة", tint = BrandPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            if (cartItemCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = SurfaceCard
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        AppButton(
                            text = "عرض السلة ($cartItemCount صنف) ⚡",
                            onClick = onViewCart,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.ShoppingBag,
                            testTag = "partner_bottom_cart_btn"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceBackground),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Partner Header Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = partner.logoEmoji, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = partner.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${partner.type.titleArabic} • ${partner.address}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = SurfaceBorder)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${partner.rating} (ممتاز)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${partner.deliveryTimeMinutes} دقيقة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${partner.deliveryFee.toInt()} ج.م توصيل",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Category Scrollable Tabs
            if (categories.isNotEmpty()) {
                item {
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        containerColor = SurfaceCard,
                        contentColor = BrandPrimary,
                        edgePadding = 16.dp,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Tab(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                text = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                item {
                    EmptyState(
                        title = "لا توجد أصناف",
                        message = "لا توجد أصناف مسجلة في هذا التصنيف حاليًا.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(filteredProducts) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { productForSheet = prod }
                            .testTag("product_item_${prod.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = prod.imageEmoji, fontSize = 32.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = prod.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${prod.price.toInt()} ج.م",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = BrandPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة للسلة",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Product Options Bottom Sheet
    if (productForSheet != null) {
        val prod = productForSheet!!

        // Parse sizes before initializing the selection so the first valid size
        // is always the default instead of an artificial "عادي" value.
        val sizeOptions = remember(prod) {
            prod.sizesString.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) Pair(parts[0], parts[1].toDoubleOrNull() ?: 0.0) else null
            }
        }

        var quantity by remember(prod.id) { mutableIntStateOf(1) }
        var selectedSize by remember(prod.id, sizeOptions) {
            mutableStateOf(sizeOptions.firstOrNull()?.first.orEmpty())
        }
        var sizePriceOffset by remember(prod.id, sizeOptions) {
            mutableDoubleStateOf(sizeOptions.firstOrNull()?.second ?: 0.0)
        }
        val selectedAddons = remember(prod.id) { mutableStateListOf<String>() }
        var specialNotes by remember(prod.id) { mutableStateOf("") }

        // Parse addons
        val addonOptions = remember(prod) {
            prod.addonsString.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) Pair(parts[0], parts[1].toDoubleOrNull() ?: 0.0) else null
            }
        }

        ModalBottomSheet(
            onDismissRequest = { productForSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = prod.imageEmoji, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${prod.price.toInt()} ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sizes
                if (sizeOptions.isNotEmpty()) {
                    Text(
                        text = "اختر الحجم:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sizeOptions.forEach { (name, price) ->
                            val isChosen = selectedSize == name
                            FilterChip(
                                selected = isChosen,
                                onClick = {
                                    selectedSize = name
                                    sizePriceOffset = price
                                },
                                label = {
                                    Text(
                                        text = if (price > 0) "$name (+${price.toInt()})" else name,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Addons
                if (addonOptions.isNotEmpty()) {
                    Text(
                        text = "إضافات اختيارية:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    addonOptions.forEach { (addonName, addonPrice) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedAddons.contains(addonName)) selectedAddons.remove(addonName)
                                    else selectedAddons.add(addonName)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedAddons.contains(addonName),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) selectedAddons.add(addonName)
                                        else selectedAddons.remove(addonName)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandPrimary)
                                )
                                Text(addonName, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("+${addonPrice.toInt()} ج.م", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Special notes
                AppInput(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    label = "ملاحظات خاصة (اختياري)",
                    placeholder = "مثال: صوص زيادة، بدون بصل...",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Quantity & Add to Cart Action.
                // Size and selected add-ons are part of the actual line-item price.
                val selectedAddonsPrice = selectedAddons.sumOf { selectedName ->
                    addonOptions.firstOrNull { it.first == selectedName }?.second ?: 0.0
                }
                val configuredUnitPrice = prod.price + sizePriceOffset + selectedAddonsPrice
                val totalProductPrice = configuredUnitPrice * quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceBackground)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "تقليل")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }

                    // Add Button
                    AppButton(
                        text = "إضافة (${totalProductPrice.toInt()} ج.م) ⚡",
                        onClick = {
                            val optionsList = mutableListOf<String>()
                            if (selectedSize.isNotEmpty()) {
                                val sizeLabel = if (sizePriceOffset > 0) {
                                    "حجم: $selectedSize (+${sizePriceOffset.toInt()} ج.م)"
                                } else {
                                    "حجم: $selectedSize"
                                }
                                optionsList.add(sizeLabel)
                            }
                            if (selectedAddons.isNotEmpty()) {
                                val addonsLabel = selectedAddons.joinToString("، ") { selectedName ->
                                    val price = addonOptions.firstOrNull { it.first == selectedName }?.second ?: 0.0
                                    if (price > 0) "$selectedName (+${price.toInt()} ج.م)" else selectedName
                                }
                                optionsList.add("إضافات: $addonsLabel")
                            }
                            if (specialNotes.isNotBlank()) optionsList.add("ملاحظة: $specialNotes")
                            val optionsSummary = optionsList.joinToString(" • ")

                            // A configured copy lets the cart distinguish two variants
                            // of the same product and keeps the charged unit price correct.
                            val configuredProduct = prod.copy(price = configuredUnitPrice)
                            onAddToCart(configuredProduct, quantity, optionsSummary)
                            productForSheet = null
                        },
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        testTag = "modal_add_to_cart_btn"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
