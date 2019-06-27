package mx.uv.transito.data.entity

import java.io.Serializable
import java.util.*

data class Conductor(val idConductor: Int,
                     var nombre: String,
                     var apellidoPaterno: String,
                     var apellidoMaterno: String,
                     var fechaNacimiento: Date,
                     var numLicencia: Int,
                     var numCelular: String,
                     var contrasenia: String): Serializable