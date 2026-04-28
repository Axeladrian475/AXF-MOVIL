package com.axf.gymnet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver que arranca el ChatSocketService cuando:
 *  - El teléfono termina de reiniciarse (BOOT_COMPLETED)
 *  - La app se actualiza (MY_PACKAGE_REPLACED)
 *
 * Esto garantiza que el usuario siga recibiendo mensajes incluso si
 * apagó y encendió el teléfono sin abrir la app.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val accion = intent.action ?: return

        if (accion == Intent.ACTION_BOOT_COMPLETED ||
            accion == Intent.ACTION_MY_PACKAGE_REPLACED) {

            // Solo iniciar el servicio si hay sesión activa
            val prefs = context.getSharedPreferences("axf_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)

            if (!token.isNullOrEmpty()) {
                ChatSocketService.start(context)
            }
        }
    }
}