package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.axf.gymnet.data.FcmTokenRequest
import com.axf.gymnet.network.RetrofitClient
import com.axf.gymnet.viewmodel.LoginViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var etEmail:    EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin:   Button
    private lateinit var tvError:    TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay sesión activa, saltar directo a MainActivity
        val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
        if (!prefs.getString("token", null).isNullOrEmpty()) {
            // Asegurar que el servicio esté corriendo
            ChatSocketService.start(this)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // Crear canales de notificación (necesario en Android 8+)
        MyFirebaseMessagingService.crearCanales(this)

        etEmail     = findViewById(R.id.etEmail)
        etPassword  = findViewById(R.id.etPassword)
        btnLogin    = findViewById(R.id.btnLogin)
        tvError     = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            tvError.visibility = View.GONE
            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }

        viewModel.loginResult.observe(this) { response ->
            response?.let {
                if (it.success && it.suscriptor != null && it.token != null) {
                    // Guardar sesión
                    prefs.edit()
                        .putInt("userId",              it.suscriptor.id)
                        .putString("userName",         it.suscriptor.nombres)
                        .putString("userApellido",     it.suscriptor.apellidoPaterno)
                        .putInt("sucursalId",          it.suscriptor.sucursalId)
                        .putString("token",            it.token)
                        .putBoolean("suscripcionActiva", it.suscriptor.suscripcionActiva)
                        .putString("fechaVencimiento", it.suscriptor.fechaVencimiento ?: "")
                        .apply()

                    // Registrar FCM token con el nuevo authToken
                    registrarFcmToken(it.token)

                    // Iniciar servicio de mensajes en background
                    ChatSocketService.start(this)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {
                    tvError.text = it.message ?: "Credenciales incorrectas"
                    tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                tvError.text = it
                tvError.visibility = View.VISIBLE
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !loading
        }
    }

    private fun registrarFcmToken(authToken: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            getSharedPreferences("axf_prefs", MODE_PRIVATE)
                .edit().putString("fcm_token", fcmToken).apply()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.instance.registrarFcmToken(
                        "Bearer $authToken",
                        FcmTokenRequest(fcmToken)
                    )
                } catch (_: Exception) {}
            }
        }
    }
}
