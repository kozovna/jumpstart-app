package ge.merabi.jumpstart.util

import ge.merabi.jumpstart.data.Order
import java.text.SimpleDateFormat
import java.util.*

object OrderTextGenerator {

    fun generate(order: Order): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = timeFormat.format(Date(order.timestamp))
        val tariffLabel = if (order.isNight) "ღამე" else "დღე"
        val tariffIcon = if (order.isNight) "🌙" else "☀️"

        val sb = StringBuilder()
        sb.append("🚗 ახალი შეკვეთა — ავტომობილის დაქოქვა\n\n")
        sb.append("⏰ დრო: $timeStr\n")
        sb.append("$tariffIcon ტარიფი: $tariffLabel\n")
        sb.append("💰 ფასი: ${formatPrice(order.price)} ₾\n\n")

        sb.append("👤 კლიენტი: ${order.clientName}\n")
        sb.append("📞 ტელ: ${order.clientPhone}\n\n")

        val carLine = listOf(order.carBrand, order.carModel).filter { it.isNotBlank() }.joinToString(" ")
        sb.append("🚘 ავტომობილი: $carLine\n")
        val engineLine = listOf(order.engineVolume, order.engineType).filter { it.isNotBlank() }.joinToString(" ")
        sb.append("⛽ ძრავი: $engineLine\n")
        sb.append("🔢 ნომერი: ${order.plateNumber}\n")
        if (order.carColor.isNotBlank()) {
            sb.append("🎨 ფერი: ${order.carColor}\n")
        }
        sb.append("\n")

        sb.append("📍 უბანი: ${order.district}\n")
        if (order.exactAddress.isNotBlank()) {
            sb.append("📌 მისამართი: ${order.exactAddress}\n")
        }
        if (order.mapsLink.isNotBlank()) {
            sb.append("\n🗺️ ლოკაცია: ${order.mapsLink}\n")
        }

        if (order.additionalInfo.isNotBlank()) {
            sb.append("\n📝 დამატებითი ინფორმაცია:\n${order.additionalInfo}\n")
        }

        sb.append("\n📌 სტატუსი: ${order.status}")

        return sb.toString()
    }

    private fun formatPrice(price: Double): String {
        return if (price == price.toLong().toDouble()) {
            price.toLong().toString()
        } else {
            price.toString()
        }
    }
}
