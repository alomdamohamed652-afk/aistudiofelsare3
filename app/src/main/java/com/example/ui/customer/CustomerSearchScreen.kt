package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.example.core.model.PartnerType
import com.example.data.local.PartnerEntity
import com.example.data.local.ProductEntity

@Composable
fun CustomerSearchScreen(
    partners: List<PartnerEntity>,
    products: List<ProductEntity>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: PartnerType?,
    onCategorySelect: (PartnerType?) -> Unit,
    onlyOpenFilter: Boolean,
    onToggleOnlyOpen: () -> Unit,
    onPartnerClick: (PartnerEntity) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPartners = remember(partners, searchQuery, selectedCategory, onlyOpenFilter) {
        partners.filter { p ->
            val matchQuery = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == null || p.type == selectedCategory
            val matchOpen = !onlyOpenFilter || p.isOpen
            matchQuery && matchCat && matchOpen
        }
    }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(top = 16.dp)
    ) {
        // Search bar
        AppInput(
            value = searchQuery,
            onValueChange = onQueryChange,
            label = "البحث في فالسريع",
            placeholder = "ابحث عن مطعم، صيدلية، أو وجبة...",
            leadingIcon = Icons.Default.Search,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            testTag = "search_screen_input"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Horizontal Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("الكل", fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal) }
                )
            }
            items(PartnerType.values()) { type ->
                val isSelected = selectedCategory == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(if (isSelected) null else type) },
                    label = {
                        Text(
                            "${type.iconEmoji} ${type.titleArabic}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = onlyOpenFilter,
                    onClick = onToggleOnlyOpen,
                    label = { Text("مفتوح الآن 🟢", fontWeight = if (onlyOpenFilter) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Products matches section if searching
            if (filteredProducts.isNotEmpty()) {
                item {
                    Text(
                        "أصناف مطابقة (${filteredProducts.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                items(filteredProducts) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(prod) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.imageEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(prod.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("${prod.price.toInt()} ج.م", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Partners matches
            item {
                Text(
                    "المتاجر والمطاعم (${filteredPartners.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (filteredPartners.isEmpty()) {
                item {
                    EmptyState(
                        title = "لا توجد نتائج",
                        message = "جرب البحث بكلمات أخرى أو تغيير الفلاتر.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(filteredPartners) { partner ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPartnerClick(partner) },
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
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(partner.logoEmoji, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(partner.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (!partner.isOpen) {
                                        StatusBadge(text = "مغلق", backgroundColor = StatusRedLight, textColor = StatusRed)
                                    }
                                }
                                Text("${partner.type.titleArabic} • ${partner.address}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("⭐ ${partner.rating}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusYellow)
                                    Text("⏱️ ${partner.deliveryTimeMinutes} دقيقة", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text("🛵 ${partner.deliveryFee.toInt()} ج.م", style = MaterialTheme.typography.labelSmall, color = BrandPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
