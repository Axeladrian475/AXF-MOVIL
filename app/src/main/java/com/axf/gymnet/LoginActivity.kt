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
import com.axf.gymnet.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail     = findViewById(R.id.etEmail)
        etPassword  = findViewById(R.id.etPassword)
        btnLogin    = findViewById(R.id.btnLogin)
        tvError     = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }

        viewModel.loginResult.observe(this) { response ->
            response?.let {
                if (it.success && it.suscriptor != null) {
                    val prefs = getSharedPreferences("axf_prefs", MODE_PRIVATE)
                    prefs.edit()
                        .putInt("userId", it.suscriptor.id)
                        .putString("userName", it.suscriptor.nombres)
                        .putString("token", it.token)
                        .putBoolean("suscripcionActiva", it.suscriptor.suscripcionActiva)
                        .putString("fechaVencimiento", it.suscriptor.fechaVencimiento ?: "")
                        .apply()

                    // Registrar FCM token en backend
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { fcmToken ->
                            prefs.edit().putString("fcm_token", fcmToken).apply()
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    com.axf.gymnet.network.RetrofitClient.instance.registrarFcmToken(
                                        "Bearer ${it.token}",
                                        com.axf.gymnet.data.FcmTokenRequest(fcmToken)
                                    )
                                } catch (_: Exception) {}
                            }
                        }

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    tvError.text = it.message
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
}