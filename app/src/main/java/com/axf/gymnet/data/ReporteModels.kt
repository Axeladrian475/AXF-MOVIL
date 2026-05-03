package com.axf.gymnet.data

// ── Respuesta de sucursales ───────────────────────────────────────────────────
data class SucursalItem(
    val id_sucursal: Int,
    val nombre: String
)

// ── Respuesta de personal (para reporte de personal) ─────────────────────────
data class PersonalItem(
    val id_personal: Int,
    val nombres: String,
    val apellido_paterno: String,
    val puesto: String,
    val foto_url: String?
)

// ── Request para crear reporte ────────────────────────────────────────────────
data class CrearReporteRequest(
    val id_sucursal: Int,
    val categoria: String,       // "Maquina_Dañada" | "Baño_Tapado" | "Problema_Limpieza" | "Reporte_Personal" | "Otro"
    val descripcion: String,
    val es_privado: Boolean,
    val id_personal_reportado: Int? = null,
    val sobre_atencion_previa: Boolean? = null
)

// ── Response al crear reporte ─────────────────────────────────────────────────
data class CrearReporteResponse(
    val success: Boolean,
    val message: String,
    val id_reporte: Int?
)

// ── Reporte público (para lista) ──────────────────────────────────────────────
data class ReportePublico(
    val id_reporte: Int,
    val categoria: String,
    val descripcion: String,
    val foto_url: String?,
    val estado: String,          // "Abierto" | "En_Proceso" | "Resuelto"
    val num_strikes: Int,
    val creado_en: String,
    val sumados: Int,            // cuántos se sumaron
    val ya_sumado: Int            // MySQL tinyint: 0 ó 1 — si el usuario ya se sumó
)

// ── Response de lista de reportes públicos ─────────────────────────────────────
data class ReportesPublicosResponse(
    val success: Boolean,
    val reportes: List<ReportePublico>
)

// ── Request para sumarse ──────────────────────────────────────────────────────
data class SumarseReporteResponse(
    val success: Boolean,
    val message: String
)

// ── Verificar atención previa ─────────────────────────────────────────────────
data class AtencionPreviaResponse(
    val success: Boolean,
    val tuvo_atencion: Int   // MySQL tinyint: 0 ó 1
)

// ── Mis reportes ──────────────────────────────────────────────────────────────
data class MiReporte(
    val id_reporte: Int,
    val id_sucursal: Int,
    val nombre_sucursal: String,
    val categoria: String,
    val descripcion: String,
    val foto_url: String?,
    val es_privado: Int,          // MySQL tinyint devuelve 0/1, no true/false
    val estado: String,
    val num_strikes: Int,
    val creado_en: String,
    val nombre_personal_reportado: String?
)

data class MisReportesResponse(
    val success: Boolean,
    val reportes: List<MiReporte>
)