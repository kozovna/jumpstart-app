package ge.merabi.jumpstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ge.merabi.jumpstart.data.AppDatabase
import ge.merabi.jumpstart.data.OrderRepository
import ge.merabi.jumpstart.navigation.AppNavigation
import ge.merabi.jumpstart.ui.theme.JumpStartTheme
import ge.merabi.jumpstart.viewmodel.OrderViewModel
import ge.merabi.jumpstart.viewmodel.OrderViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = OrderRepository(database.orderDao())
        val factory = OrderViewModelFactory(repository)

        setContent {
            JumpStartTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: OrderViewModel = viewModel(factory = factory)
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
