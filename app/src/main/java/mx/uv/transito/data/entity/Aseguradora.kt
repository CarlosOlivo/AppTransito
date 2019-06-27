package mx.uv.transito.data.entity

data class Aseguradora(
    val idAseguradora: Int?,
    var aseguradora: String
) {
    override fun toString(): String {
        return aseguradora
    }
}