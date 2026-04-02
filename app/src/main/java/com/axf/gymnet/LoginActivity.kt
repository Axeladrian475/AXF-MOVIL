package com.axf.gymnet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.axf.gymnet.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvError = findViewById<TextView>(R.id.tvError)

        btnLogin.setOnClickListener {
            tvError.visibility = View.GONE
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
            btnLogin.isEnabled = !loading
            btnLogin.text = if (loading) "Cargando..." else "Iniciar Sesión"
        }
    }
}