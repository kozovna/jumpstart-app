package ge.merabi.jumpstart.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import kotlinx.coroutines.tasks.await

object LocationUtil {

    /**
     * აბრუნებს Google Maps ბმულს ("https://www.google.com/maps?q=lat,lng") მიმდინარე
     * GPS ლოკაციის მიხედვით, ან null-ს თუ ლოკაციის მიღება ვერ მოხერხდა.
     * წინაპირობა: ACCESS_FINE_LOCATION ან ACCESS_COARSE_LOCATION ნებართვა უკვე მინიჭებულია.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationMapsLink(context: Context): String? {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            val location = client.getCurrentLocation(request, null).await() ?: return null
            "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
        } catch (e: Exception) {
            null
        }
    }
}
