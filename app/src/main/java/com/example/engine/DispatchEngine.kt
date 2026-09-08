package com.example.engine

import com.example.core.model.DeliveryStatus
import com.example.core.model.DispatchMode
import com.example.core.model.DriverStatus
import com.example.core.model.OrderStatus
import com.example.data.local.DriverProfileEntity
import com.example.data.local.OrderEntity

object DispatchEngine {

    // Cursor keeping track of the last dispatched driver ID for true Round-Robin rotation
    @Volatile
    var lastDispatchedDriverId: Long = 0L
        private set

    /**
     * Resets or sets the cursor position for testing or reconfiguration.
     */
    fun setCursor(driverId: Long) {
        lastDispatchedDriverId = driverId
    }

    /**
     * Finds candidate driver under True Round Robin, Open dispatch, or Manual assignment.
     * True Round Robin rotates: Driver A -> Driver B -> Driver C -> Driver A.
     */
    fun selectCandidateDriver(
        drivers: List<DriverProfileEntity>,
        mode: DispatchMode,
        targetWorkingArea: String? = null
    ): DriverProfileEntity? {
        val eligible = drivers.filter { isDriverEligible(it, targetWorkingArea) }
        if (eligible.isEmpty()) return null

        return when (mode) {
            DispatchMode.ROUND_ROBIN -> {
                // Find eligible drivers sorted deterministically by ID
                val sortedDrivers = eligible.sortedBy { it.id }
                
                // Find the first eligible driver whose ID is strictly greater than the cursor
                val nextDriver = sortedDrivers.firstOrNull { it.id > lastDispatchedDriverId }
                    ?: sortedDrivers.first() // Wrap around to the beginning of the queue

                // Advance the cursor
                lastDispatchedDriverId = nextDriver.id
                nextDriver
            }
            DispatchMode.OPEN_DISPATCH -> {
                // All eligible drivers are notified; candidate placeholder is the first ready
                eligible.firstOrNull()
            }
            DispatchMode.MANUAL_ASSIGNMENT -> {
                null // Admin assigns manually
            }
        }
    }

    /**
     * Strict eligibility check for a driver to receive order assignments.
     */
    fun isDriverEligible(
        driver: DriverProfileEntity,
        targetWorkingArea: String? = null
    ): Boolean {
        if (driver.status != DriverStatus.AVAILABLE) return false
        if (driver.currentOrderId != null) return false
        if (targetWorkingArea != null && driver.workingArea.isNotBlank() && !driver.workingArea.contains(targetWorkingArea, ignoreCase = true)) {
            return false
        }
        return true
    }

    /**
     * Determines whether an order should be dispatched to drivers given the current workflow settings.
     */
    fun shouldTriggerDispatch(
        order: OrderEntity,
        timingSetting: String
    ): Boolean {
        // Never dispatch cancelled, rejected, or completed orders, or orders not waiting for driver
        if (order.deliveryStatus != DeliveryStatus.WAITING_FOR_DRIVER) return false
        if (order.orderStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED, OrderStatus.DELIVERED)) return false

        return when (timingSetting) {
            "فوري عند الاعتماد" -> order.orderStatus in setOf(
                OrderStatus.APPROVED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.PICKED_UP
            )
            "عند بدء التجهيز" -> order.orderStatus in setOf(
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.PICKED_UP
            )
            "عند جاهزية الطلب" -> order.orderStatus in setOf(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.PICKED_UP
            )
            else -> true
        }
    }
}
