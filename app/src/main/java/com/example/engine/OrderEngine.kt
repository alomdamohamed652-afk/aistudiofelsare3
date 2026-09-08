package com.example.engine

import com.example.core.model.DeliveryStatus
import com.example.core.model.OrderStatus

object OrderEngine {

    /**
     * Determines whether a transition from [from] to [to] is permitted by standard rules.
     */
    fun isValidOrderStatusTransition(from: OrderStatus, to: OrderStatus): Boolean {
        if (from == to) return true
        return when (from) {
            OrderStatus.CREATED -> to in listOf(
                OrderStatus.PENDING_REVIEW,
                OrderStatus.APPROVED,
                OrderStatus.CANCELLED
            )
            OrderStatus.PENDING_REVIEW -> to in listOf(
                OrderStatus.APPROVED,
                OrderStatus.REJECTED,
                OrderStatus.CANCELLED
            )
            OrderStatus.APPROVED -> to in listOf(
                OrderStatus.PREPARING,
                OrderStatus.CANCELLED
            )
            OrderStatus.PREPARING -> to in listOf(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.CANCELLED
            )
            OrderStatus.READY_FOR_PICKUP -> to in listOf(
                OrderStatus.PICKED_UP,
                OrderStatus.CANCELLED
            )
            OrderStatus.PICKED_UP -> to == OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> false // Terminal state
            OrderStatus.REJECTED -> false  // Terminal state
            OrderStatus.CANCELLED -> false // Terminal state
        }
    }

    /**
     * Determines whether a delivery status transition is valid.
     */
    fun isValidDeliveryStatusTransition(from: DeliveryStatus, to: DeliveryStatus): Boolean {
        if (from == to) return true
        return when (from) {
            DeliveryStatus.NOT_REQUIRED -> false
            DeliveryStatus.WAITING_FOR_DRIVER -> to == DeliveryStatus.DRIVER_ASSIGNED
            DeliveryStatus.DRIVER_ASSIGNED -> to == DeliveryStatus.DRIVER_TO_PICKUP
            DeliveryStatus.DRIVER_TO_PICKUP -> to == DeliveryStatus.PICKED_UP
            DeliveryStatus.PICKED_UP -> to == DeliveryStatus.OUT_FOR_DELIVERY
            DeliveryStatus.OUT_FOR_DELIVERY -> to == DeliveryStatus.DELIVERED
            DeliveryStatus.DELIVERED -> false
        }
    }

    /**
     * Returns a humanized Arabic message for the customer representing the combined order & delivery stage.
     */
    fun getHumanizedTrackingMessage(orderStatus: OrderStatus, deliveryStatus: DeliveryStatus): String {
        return when {
            orderStatus == OrderStatus.DELIVERED || deliveryStatus == DeliveryStatus.DELIVERED ->
                "🟢 تم تسليم الطلب بنجاح! نتمنى لك تجربة ممتعة ⚡"
            orderStatus == OrderStatus.CANCELLED ->
                "🔴 تم إلغاء الطلب."
            orderStatus == OrderStatus.REJECTED ->
                "🔴 تم رفض الطلب، نعتذر لعدم توافر الأصناف المطلوبة حاليًا."
            deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY ->
                "🛵 طلبك في الطريق إليك الآن مع المندوب"
            deliveryStatus == DeliveryStatus.PICKED_UP ->
                "📦 تم استلام طلبك من المحل وجاري التوجه إليك"
            deliveryStatus == DeliveryStatus.DRIVER_TO_PICKUP ->
                "🛵 المندوب في طريقه لاستلام طلبك من الشريك"
            orderStatus == OrderStatus.READY_FOR_PICKUP ->
                "✨ طلبك جاهز وبانتظار تحرك المندوب"
            orderStatus == OrderStatus.PREPARING ->
                "🟠 جاري تجهيز طلبك بكل عناية"
            orderStatus == OrderStatus.APPROVED ->
                "🔵 تم اعتماد طلبك وسيتم البدء في تجهيزه فورًا"
            orderStatus == OrderStatus.PENDING_REVIEW ->
                "🟡 جاري مراجعة وتأكيد طلبك"
            else ->
                "⚡ تم استلام طلبك بنجاح"
        }
    }
}
