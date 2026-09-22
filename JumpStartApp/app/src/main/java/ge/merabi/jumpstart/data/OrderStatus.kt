package ge.merabi.jumpstart.data

object OrderStatus {
    const val NEW = "ახალი შეკვეთა"
    const val TRANSFERRED = "გადაცემულია ჯგუფში"
    const val ON_WAY = "ოსტატი გზაშია"
    const val ON_SITE = "ადგილზეა"
    const val COMPLETED = "დასრულებულია"
    const val CANCELLED = "გაუქმებულია"

    val ALL = listOf(NEW, TRANSFERRED, ON_WAY, ON_SITE, COMPLETED, CANCELLED)
}
