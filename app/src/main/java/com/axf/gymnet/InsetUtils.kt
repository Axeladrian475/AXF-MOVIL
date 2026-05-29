package com.axf.gymnet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Utilidades para manejar WindowInsets en Android 15 (edge-to-edge).
 *
 * Estos helpers resuelven:
 *  1. Que el contenido quede debajo de la status bar (header)
 *  2. Que la barra de nav inferior tape contenido en dispositivos
 *     con botones físicos de navegación en lugar de gestos.
 */
object InsetUtils {

    /**
     * Aplica padding TOP al header para respetar el status bar.
     * Aplica padding BOTTOM a la nav bar inferior para no tapar contenido
     * con los botones de navegación del sistema.
     *
     * @param headerView  Vista del encabezado que debe bajar debajo de la status bar.
     * @param navBar      Vista de la barra de navegación inferior. Si es null, no se ajusta.
     * @param scrollView  Vista de contenido scrolleable que también debe ajustar su padding inferior.
     */
    fun applySystemInsets(
        rootView:   View,
        headerView: View?,
        navBar:     View? = null,
        scrollView: View? = null,
        extraNavBottomPadding: Int = 0
    ) {
        // Guardamos el paddingTop original del header (definido en el XML)
        val headerOriginalPaddingTop = headerView?.paddingTop ?: 0

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // ── Header: respetar status bar ──────────────────────────────────
            headerView?.updatePadding(top = headerOriginalPaddingTop + systemBars.top)

            // ── Nav bar inferior: respetar botones de navegación ──────────────
            navBar?.let { bar ->
                bar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = systemBars.bottom
                }
            }

            // ── Contenido scrolleable: añadir padding inferior ────────────────
            // Usamos 64dp (tamaño fijo de la nav bar) + inset del sistema
            val navBarHeight = navBar?.height?.takeIf { it > 0 } ?: 0
            scrollView?.updatePadding(
                bottom = navBarHeight + systemBars.bottom + extraNavBottomPadding
            )

            windowInsets
        }
    }

    /**
     * Aplica solo el padding TOP al header (útil para activities sin nav bar propia).
     */
    fun applyTopInset(rootView: View, headerView: View?) {
        val headerOriginalPaddingTop = headerView?.paddingTop ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerView?.updatePadding(top = headerOriginalPaddingTop + systemBars.top)
            windowInsets
        }
    }

    /**
     * Solicita el permiso POST_NOTIFICATIONS en Android 13+.
     * Debe llamarse desde onCreate de cualquier Activity que muestre notificaciones.
     */
    fun AppCompatActivity.pedirPermisoNotificacionesSiNecesario(
        launcher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
