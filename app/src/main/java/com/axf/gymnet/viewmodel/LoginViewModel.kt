import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val loginResult = MutableLiveData<LoginResponse?>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage.value = "Por favor completa todos los campos"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(email.trim(), password)
                )
                if (response.isSuccessful) {
                    loginResult.value = response.body()
                } else {
                    errorMessage.value = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error de conexión. Verifica tu internet"
            } finally {
                isLoading.value = false
            }
        }
    }
}