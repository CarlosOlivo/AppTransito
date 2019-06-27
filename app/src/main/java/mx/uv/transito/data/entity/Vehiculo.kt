package mx.uv.transito.data.entity

import java.io.Serializable

data class Vehiculo(
    val placas: String,
    var idConductor: Int,
    var idAseguradora: Int? = null,
    var marca: String,
    var modelo: String,
    var anio: Int,
    var color: String,
    var poliza: String? = null
): Serializable {
    override fun toString(): String {
        return "$placas - $marca $modelo)"
    }
}