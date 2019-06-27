package mx.uv.transito.data.entity

data class Personal(
    val idPersonal: Int,
    var idCargo: Int,
    var nombre: String,
    var usuario: String,
    var contrasenia: String
)