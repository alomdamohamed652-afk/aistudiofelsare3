package com.example.engine

import com.example.core.model.DeliveryStatus
import com.example.core.model.DispatchMode
import com.example.core.model.DriverStatus
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
        if (order.deliveryStatus != DeliveryStatus.WAITING_FOR_DRIVER) return false

        return when (timingSetting) {
            "فوري عند الاعتماد" -> order.orderStatus.ordinal >= com.example.core.model.OrderStatus.APPROVED.ordinal
            "عند بدء التجهيز" -> order.orderStatus.ordinal >= com.example.core.model.OrderStatus.PREPARING.ordinal
            "عند جاهزية الطلب" -> order.orderStatus.ordinal >= com.example.core.model.OrderStatus.READY_FOR_PICKUP.ordinal
            else -> true
        }
    }
}
