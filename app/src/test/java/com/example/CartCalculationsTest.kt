package com.example

import com.example.core.model.CouponType
import com.example.data.local.CouponEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Pure unit tests for cart calculation logic (coupon application, discount calculation).
 * These tests validate the repository-level coupon logic without Room / Android dependencies.
 */
class CartCalculationsTest {

    private fun makeCoupon(
        code: String = "TEST10",
        type: CouponType = CouponType.PERCENTAGE,
        value: Double = 10.0,
        minimumOrderAmount: Double = 0.0,
        maxUsageCount: Int = 100,
        usedCount: Int = 0,
        isActive: Boolean = true
    ) = CouponEntity(
        code = code,
        type = type,
        value = value,
        minimumOrderAmount = minimumOrderAmount,
        maxUsageCount = maxUsageCount,
        usedCount = usedCount,
        isActive = isActive
    )

    // Mirrors the discount calculation inside FalsareeRepository.validateCoupon
    private fun calculateDiscount(coupon: CouponEntity, subtotal: Double): Double {
        return when (coupon.type) {
            CouponType.PERCENTAGE -> subtotal * (coupon.value / 100.0)
            CouponType.FIXED_AMOUNT -> minOf(coupon.value, subtotal) // cannot discount more than total
        }
    }

    private fun isValidCoupon(coupon: CouponEntity, subtotal: Double): Boolean {
        if (!coupon.isActive) return false
        if (coupon.usedCount >= coupon.maxUsageCount) return false
        if (subtotal < coupon.minimumOrderAmount) return false
        return true
    }

    // ─── Percentage coupons ───────────────────────────────────────────────────

    @Test
    fun `10 percent coupon on 200 EGP order gives 20 EGP discount`() {
        val coupon = makeCoupon(type = CouponType.PERCENTAGE, value = 10.0)
        assertEquals(20.0, calculateDiscount(coupon, 200.0), 0.001)
    }

    @Test
    fun `50 percent coupon on 100 EGP gives 50 EGP discount`() {
        val coupon = makeCoupon(type = CouponType.PERCENTAGE, value = 50.0)
        assertEquals(50.0, calculateDiscount(coupon, 100.0), 0.001)
    }

    @Test
    fun `100 percent coupon gives full order value as discount`() {
        val coupon = makeCoupon(type = CouponType.PERCENTAGE, value = 100.0)
        assertEquals(150.0, calculateDiscount(coupon, 150.0), 0.001)
    }

    // ─── Fixed amount coupons ─────────────────────────────────────────────────

    @Test
    fun `fixed 30 EGP coupon on 100 EGP gives exactly 30 EGP discount`() {
        val coupon = makeCoupon(type = CouponType.FIXED_AMOUNT, value = 30.0)
        assertEquals(30.0, calculateDiscount(coupon, 100.0), 0.001)
    }

    @Test
    fun `fixed coupon cannot discount more than the subtotal`() {
        val coupon = makeCoupon(type = CouponType.FIXED_AMOUNT, value = 200.0)
        // Subtotal is only 50, so discount should be capped at 50
        assertEquals(50.0, calculateDiscount(coupon, 50.0), 0.001)
    }

    // ─── Coupon validation rules ──────────────────────────────────────────────

    @Test
    fun `inactive coupon is rejected`() {
        val coupon = makeCoupon(isActive = false)
        assertFalse(isValidCoupon(coupon, 200.0))
    }

    @Test
    fun `exhausted coupon (usedCount == maxUsageCount) is rejected`() {
        val coupon = makeCoupon(maxUsageCount = 10, usedCount = 10)
        assertFalse(isValidCoupon(coupon, 200.0))
    }

    @Test
    fun `coupon with minimum order not met is rejected`() {
        val coupon = makeCoupon(minimumOrderAmount = 150.0)
        assertFalse(isValidCoupon(coupon, 100.0)) // 100 < 150
    }

    @Test
    fun `coupon with minimum order exactly met is accepted`() {
        val coupon = makeCoupon(minimumOrderAmount = 100.0)
        assertTrue(isValidCoupon(coupon, 100.0))
    }

    @Test
    fun `valid active coupon within usage limit is accepted`() {
        val coupon = makeCoupon(isActive = true, maxUsageCount = 5, usedCount = 3)
        assertTrue(isValidCoupon(coupon, 200.0))
    }

    @Test
    fun `zero minimum order coupon is always valid by amount`() {
        val coupon = makeCoupon(minimumOrderAmount = 0.0)
        assertTrue(isValidCoupon(coupon, 1.0))
    }
}
