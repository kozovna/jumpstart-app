package ge.merabi.jumpstart.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val dao: OrderDao) {

    val allOrders: Flow<List<Order>> = dao.getAllOrders()

    suspend fun insert(order: Order): Long = dao.insert(order)

    suspend fun update(order: Order) = dao.update(order)

    suspend fun delete(order: Order) = dao.delete(order)

    suspend fun getById(id: Long): Order? = dao.getOrderById(id)
}
