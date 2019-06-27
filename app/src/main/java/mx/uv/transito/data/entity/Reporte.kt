package mx.uv.transito.data.entity

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Reporte(
    val idReporte: Int? = null,
    var idConductor: Int,
    var idImplicado: Int,
    var idDictamen: Int? = null,
    var imgs: Int = 0,
    var placas: String,
    var latitud: Double,
    var longitud: Double,
    var fechaHora: Date? = null
): Serializable {
    override fun toString(): String {
        return "#$idReporte - ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.getDefault()).format(fechaHora)}"
    }
}