package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*
import com.example.core.model.HomeSectionType
import com.example.core.model.PartnerType
import com.example.data.local.HomeSectionEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProductEntity

@Composable
fun CustomerHomeScreen(
    sections: List<HomeSectionEntity>,
    partners: List<PartnerEntity>,
    products: List<ProductEntity>,
    onPartnerClick: (PartnerEntity) -> Unit,
    onCategoryClick: (PartnerType) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int,
    onNotificationClick: () -> Unit,
    unreadNotificationCount: Int,
    favoritePartnerIds: List<Long> = emptyList(),
    onToggleFavorite: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Bar with Location & Actions
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceCard,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "التوصيل إلى",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "المهندسين، شارع سوريا 📍",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Actions: Notifications & Cart
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.testTag("home_notification_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(containerColor = StatusRed) {
                                            Text("$unreadNotificationCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "الإشعارات",
                                    tint = TextPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier.testTag("home_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(containerColor = BrandPrimary) {
                                            Text("$cartItemCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "السلة",
                                    tint = BrandPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Search Bar Trigger
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(onClick = onSearchClick)
                    .testTag("home_search_bar_trigger"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ابحث عن مطعم، صيدلية، كافيه، أو صنف...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        }

        // 3. Dynamic Sections Rendered According to Home Builder Config
        items(sections.filter { it.active }) { section ->
            when (section.type) {
                HomeSectionType.BANNERS -> {
                    BannerCarousel()
                }
                HomeSectionType.CATEGORIES -> {
                    CategoriesSection(onCategoryClick = onCategoryClick)
                }
                HomeSectionType.OFFERS -> {
                    OffersSection(
                        partners = partners.take(section.limitCount),
                        onPartnerClick = onPartnerClick,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                HomeSectionType.NEARBY_PARTNERS -> {
                    PartnerHorizontalList(
                        title = section.title,
                        partners = partners.sortedBy { it.distanceKm }.take(section.limitCount),
                        onPartnerClick = onPartnerClick,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                HomeSectionType.TOP_RATED -> {
                    PartnerHorizontalList(
                        title = section.title,
                        partners = partners.sortedByDescending { it.rating }.take(section.limitCount),
                        onPartnerClick = onPartnerClick,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                HomeSectionType.FEATURED_PRODUCTS -> {
                    FeaturedProductsSection(
                        title = section.title,
                        products = products.take(section.limitCount),
                        onProductClick = onProductClick
                    )
                }
                else -> {
                    PartnerHorizontalList(
                        title = section.title,
                        partners = partners.take(section.limitCount),
                        onPartnerClick = onPartnerClick,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
fun BannerCarousel() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .width(310.dp)
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(BrandPrimary, BrandPrimaryDark)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "⚡ عرض فالسريع الحصري",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "خصم 30% على أول طلبين",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استخدم كود: SARIEE30",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .width(310.dp)
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(BrandSecondary, BrandSecondaryLight)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StatusGreen.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "💊 صيدليات 24 ساعة",
                                color = StatusGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "أدويتك ومستلزماتك توصلك فورًا",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توصيل آمن وبأعلى سرعة 🛵",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(onCategoryClick: (PartnerType) -> Unit) {
    val itemsList = listOf(
        Triple("مطاعم", "🍔", PartnerType.RESTAURANT),
        Triple("صيدليات", "💊", PartnerType.PHARMACY),
        Triple("كافيهات", "☕", PartnerType.CAFE),
        Triple("متاجر", "🛒", PartnerType.STORE),
        Triple("هدايا", "🎁", PartnerType.STORE),
        Triple("عروض", "🏷️", PartnerType.RESTAURANT),
        Triple("خدمات أخرى", "📦", PartnerType.STORE),
        Triple("المزيد", "➕", PartnerType.RESTAURANT)
    )

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = "التصنيفات والخدمات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsList) { (title, emoji, type) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCategoryClick(type) }
                        .padding(4.dp)
                        .testTag("category_chip_$title")
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun OffersSection(
    partners: List<PartnerEntity>,
    onPartnerClick: (PartnerEntity) -> Unit,
    favoritePartnerIds: List<Long> = emptyList(),
    onToggleFavorite: (Long) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥 عروض حصرية وقريب منك",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "توصيل سريع ⚡",
                style = MaterialTheme.typography.labelSmall,
                color = CustomerPrimaryBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(partners) { partner ->
                PartnerCard(
                    partner = partner,
                    onClick = { onPartnerClick(partner) },
                    isFavorite = favoritePartnerIds.contains(partner.id),
                    onToggleFavorite = { onToggleFavorite(partner.id) }
                )
            }
        }
    }
}

@Composable
fun PartnerHorizontalList(
    title: String,
    partners: List<PartnerEntity>,
    onPartnerClick: (PartnerEntity) -> Unit,
    favoritePartnerIds: List<Long> = emptyList(),
    onToggleFavorite: (Long) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(partners) { partner ->
                PartnerCard(
                    partner = partner,
                    onClick = { onPartnerClick(partner) },
                    isFavorite = favoritePartnerIds.contains(partner.id),
                    onToggleFavorite = { onToggleFavorite(partner.id) }
                )
            }
        }
    }
}

@Composable
fun PartnerCard(
    partner: PartnerEntity,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("partner_card_${partner.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = partner.logoEmoji, fontSize = 24.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusYellowLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${partner.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    FavoriteIconButton(
                        isFavorite = isFavorite,
                        onToggle = onToggleFavorite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = partner.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = partner.type.titleArabic,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = SurfaceBorder.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${partner.deliveryTimeMinutes} دقيقة",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${partner.deliveryFee.toInt()} ج.م توصيل",
                    style = MaterialTheme.typography.labelSmall,
                    color = CustomerPrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FeaturedProductsSection(
    title: String,
    products: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { onProductClick(product) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = product.imageEmoji, fontSize = 38.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${product.price.toInt()} ج.م",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )

                            Surface(
                                shape = CircleShape,
                                color = BrandPrimary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
