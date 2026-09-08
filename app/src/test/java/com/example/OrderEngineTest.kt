package com.example

import com.example.core.model.DeliveryStatus
import com.example.core.model.OrderStatus
import com.example.engine.OrderEngine
import org.junit.Assert.*
import org.junit.Test

class OrderEngineTest {

    // ─── OrderStatus transitions ──────────────────────────────────────────────

    @Test
    fun `CREATED can transition to PENDING_REVIEW`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.CREATED, OrderStatus.PENDING_REVIEW))
    }

    @Test
    fun `CREATED can transition to APPROVED directly`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.CREATED, OrderStatus.APPROVED))
    }

    @Test
    fun `CREATED can be CANCELLED`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.CREATED, OrderStatus.CANCELLED))
    }

    @Test
    fun `CREATED cannot skip directly to PREPARING`() {
        assertFalse(OrderEngine.isValidOrderStatusTransition(OrderStatus.CREATED, OrderStatus.PREPARING))
    }

    @Test
    fun `PENDING_REVIEW can be APPROVED`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.PENDING_REVIEW, OrderStatus.APPROVED))
    }

    @Test
    fun `PENDING_REVIEW can be REJECTED`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.PENDING_REVIEW, OrderStatus.REJECTED))
    }

    @Test
    fun `APPROVED can transition to PREPARING`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.APPROVED, OrderStatus.PREPARING))
    }

    @Test
    fun `APPROVED cannot jump to DELIVERED`() {
        assertFalse(OrderEngine.isValidOrderStatusTransition(OrderStatus.APPROVED, OrderStatus.DELIVERED))
    }

    @Test
    fun `PREPARING moves to READY_FOR_PICKUP`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP))
    }

    @Test
    fun `PICKED_UP moves to DELIVERED`() {
        assertTrue(OrderEngine.isValidOrderStatusTransition(OrderStatus.PICKED_UP, OrderStatus.DELIVERED))
    }

    @Test
    fun `DELIVERED is terminal - no further transitions`() {
        for (next in OrderStatus.entries) {
            if (next == OrderStatus.DELIVERED) continue
            assertFalse(
                "Expected DELIVERED -> $next to be invalid",
                OrderEngine.isValidOrderStatusTransition(OrderStatus.DELIVERED, next)
            )
        }
    }

    @Test
    fun `CANCELLED is terminal - no further transitions`() {
        for (next in OrderStatus.entries) {
            if (next == OrderStatus.CANCELLED) continue
            assertFalse(
                "Expected CANCELLED -> $next to be invalid",
                OrderEngine.isValidOrderStatusTransition(OrderStatus.CANCELLED, next)
            )
        }
    }

    @Test
    fun `REJECTED is terminal - no further transitions`() {
        for (next in OrderStatus.entries) {
            if (next == OrderStatus.REJECTED) continue
            assertFalse(
                "Expected REJECTED -> $next to be invalid",
                OrderEngine.isValidOrderStatusTransition(OrderStatus.REJECTED, next)
            )
        }
    }

    @Test
    fun `same-state transition is always valid`() {
        for (status in OrderStatus.entries) {
            assertTrue(OrderEngine.isValidOrderStatusTransition(status, status))
        }
    }

    // ─── DeliveryStatus transitions ───────────────────────────────────────────

    @Test
    fun `WAITING_FOR_DRIVER can assign a driver`() {
        assertTrue(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.WAITING_FOR_DRIVER, DeliveryStatus.DRIVER_ASSIGNED))
    }

    @Test
    fun `DRIVER_ASSIGNED can skip directly to OUT_FOR_DELIVERY`() {
        assertTrue(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.DRIVER_ASSIGNED, DeliveryStatus.OUT_FOR_DELIVERY))
    }

    @Test
    fun `DRIVER_ASSIGNED can release back to WAITING`() {
        assertTrue(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.DRIVER_ASSIGNED, DeliveryStatus.WAITING_FOR_DRIVER))
    }

    @Test
    fun `OUT_FOR_DELIVERY can only move to DELIVERED`() {
        assertTrue(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.DELIVERED))
        assertFalse(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.WAITING_FOR_DRIVER))
        assertFalse(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.DRIVER_ASSIGNED))
    }

    @Test
    fun `DELIVERED is terminal delivery state`() {
        for (next in DeliveryStatus.entries) {
            if (next == DeliveryStatus.DELIVERED) continue
            assertFalse(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.DELIVERED, next))
        }
    }

    @Test
    fun `NOT_REQUIRED is terminal delivery state`() {
        for (next in DeliveryStatus.entries) {
            if (next == DeliveryStatus.NOT_REQUIRED) continue
            assertFalse(OrderEngine.isValidDeliveryStatusTransition(DeliveryStatus.NOT_REQUIRED, next))
        }
    }
}
