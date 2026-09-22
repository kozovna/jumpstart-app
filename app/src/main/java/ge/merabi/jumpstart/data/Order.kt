package ge.merabi.jumpstart.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,          // System.currentTimeMillis() at creation
    val clientName: String,
    val clientPhone: String,
    val carBrand: String,
    val carModel: String,
    val engineType: String,
    val engineVolume: String,
    val plateNumber: String,
    val carColor: String,
    val district: String,
    val exactAddress: String,
    val mapsLink: String,
    val isNight: Boolean,
    val price: Double,
    val additionalInfo: String,
    val status: String
)
