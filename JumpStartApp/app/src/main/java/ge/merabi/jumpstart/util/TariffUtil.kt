package ge.merabi.jumpstart.util

import java.util.Calendar

/**
 * ღამის/დღის ტარიფის ავტომატური განსაზღვრის ლოგიკა.
 * 00:00–07:59 -> ღამე -> 100 ₾ (ფიქსირებული, მაგრამ ოპერატორს შეუძლია ხელით შეცვლა)
 * 08:00–23:59 -> დღე -> ოპერატორი ირჩევს ფასს (30/35/40/45/50 ₾ ან ხელით)
 */
object TariffUtil {

    const val NIGHT_PRICE = 100.0
    val DAY_PRICE_OPTIONS = listOf(30.0, 35.0, 40.0, 45.0, 50.0)

    fun isNightNow(calendar: Calendar = Calendar.getInstance()): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in 0..7
    }

    fun isNightForHour(hour: Int): Boolean = hour in 0..7
}
