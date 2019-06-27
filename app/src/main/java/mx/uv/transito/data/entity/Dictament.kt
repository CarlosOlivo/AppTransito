package mx.uv.transito.data.entity

import java.util.*

data class Dictamen(
    val idDictamen: Int,
    var descripcion: String,
    var fechaHora: Date,
    var idPersonal: Int)