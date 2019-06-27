package mx.uv.transito.data.entity

data class Implicado(
    val idImplicado: Int? = null,
    var idAseguradora: Int? = null,
    var nombre: String? = null,
    var placas: String,
    var poliza: String? = null,
    var marca: String,
    var modelo: String,
    var color: String
)