package com.axf.gymnet

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Clase Application global de AxF GymNet.
 *
 * Crea los canales de notificación lo más pronto posible — antes de que
 * cualquier FCM message llegue con la app cerrada — y antes de que
 * onCreate() de cualquier Activity se ejecute.
 *
 * Android 15 exige que los canales existan ANTES de llamar notify().
 *
 * También registra un ActivityLifecycleCallbacks para trackear si
 * la app está en foreground y cuál Activity está visible — necesario
 * para suprimir notificaciones duplicadas cuando ChatActivity está abierto.
 */
class AxFApp : Application() {

    companion object {
        /** Indica si la app está en foreground (alguna Activity visible). */
        @Volatile
        var isAppInForeground: Boolean = false
            private set

        /** Activity actualmente visible (o null si app en background). */
        @Volatile
        var currentActivity: Activity? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // Registrar canales en el arranque más temprano posible.
        // Llamar aquí garantiza que estén disponibles incluso cuando
        // MyFirebaseMessagingService se despierta con la app cerrada.
        MyFirebaseMessagingService.crearCanales(this)

        // Trackear estado de foreground/background
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0

            override fun onActivityStarted(activity: Activity) {
                activityCount++
                isAppInForeground = true
                currentActivity = activity
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount <= 0) {
                    isAppInForeground = false
                    currentActivity = null
                    activityCount = 0
                }
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                // Si ChatActivity está visible, notificar al ChatSocketService
                if (activity is ChatActivity) {
                    val idPersonal = activity.intent?.getIntExtra("id_personal", 0) ?: 0
                    if (idPersonal > 0) {
                        ChatSocketService.activeChatPersonalId = idPersonal
                    }
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity is ChatActivity) {
                    ChatSocketService.activeChatPersonalId = null
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
