package ge.merabi.jumpstart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.merabi.jumpstart.data.Order
import ge.merabi.jumpstart.data.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    val orders: StateFlow<List<Order>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertOrder(order: Order, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insert(order)
            onDone(id)
        }
    }

    fun updateOrder(order: Order, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(order)
            onDone()
        }
    }

    fun deleteOrder(order: Order, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(order)
            onDone()
        }
    }

    suspend fun getOrder(id: Long): Order? = repository.getById(id)
}
