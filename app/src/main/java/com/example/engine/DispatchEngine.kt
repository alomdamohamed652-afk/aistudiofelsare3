package com.example.engine

import com.example.core.model.DeliveryStatus
import com.example.core.model.DispatchMode
import com.example.core.model.DriverStatus
import com.example.core.model.OrderStatus
import com.example.data.local.DriverProfileEntity
import com.example.data.local.OrderEntity

object DispatchEngine {

    /**
     * Finds candidate driver under Round Robin or Open dispatch.
     */
    fun selectCandidateDriver(
        drivers: List<DriverProfileEntity>,
        mode: DispatchMode
    ): DriverProfileEntity? {
        val available = drivers.filter { it.status == DriverStatus.AVAILABLE }
        if (available.isEmpty()) return null

        return when (mode) {
            DispatchMode.ROUND_ROBIN -> {
                // Round robin prioritizes available driver with least completed orders or first in queue
                available.minByOrNull { it.completedOrdersCount } ?: available.first()
            }
            DispatchMode.OPEN_DISPATCH -> {
                // Any available driver can accept
                available.firstOrNull()
            }
            DispatchMode.MANUAL_ASSIGNMENT -> {
                null // Admin assigns manually
            }
        }
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
