package mx.uv.transito.ui.vehiculo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_vehiculos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Conductor
import mx.uv.transito.data.entity.Mensaje
import mx.uv.transito.data.entity.Vehiculo
import org.jetbrains.anko.*

class VehiculosActivity : AppCompatActivity() {

    lateinit var conductor: Conductor
    lateinit var apiService: ApiService
    lateinit var listaVehiculos: List<Vehiculo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehiculos)
        conductor = intent.extras!!.get("conductor") as Conductor
        apiService = ApiService()
        lst_vehiculos.emptyView = empty
        lst_vehiculos.setOnItemClickListener { parent, view, position, id ->
            detalleVehiculo(position)
        }
        lst_vehiculos.setOnItemLongClickListener { parent, view, position, id ->
            eliminarVehiculo(position)
            return@setOnItemLongClickListener true
        }
        cargarVehiculos()
    }

    private fun cargarVehiculos() {
        GlobalScope.launch(Dispatchers.Default) {
            val vehiculos = apiService.getVehiculosConductor(conductor.idConductor).execute()
            if (vehiculos.body() != null) {
                listaVehiculos = vehiculos.body() as List<Vehiculo>
                this.launch(Dispatchers.Main) {
                    lst_vehiculos.adapter = ArrayAdapter<Vehiculo>(
                        this@VehiculosActivity,
                        android.R.layout.simple_list_item_1,
                        listaVehiculos
                    )
                }
            }
        }
    }

    private fun detalleVehiculo(index: Int) {
        val vehiculo = listaVehiculos[index]
        startActivityForResult(intentFor<VehiculoActivity>(
            "conductor" to conductor,
            "vehiculo" to vehiculo
        ), 0)
    }

    private fun eliminarVehiculo(index: Int) {
        val vehiculo = listaVehiculos[index]
        alert("¿Desea eliminar el vehiculo?") {
            yesButton {
                GlobalScope.launch(Dispatchers.Default) {
                    val mensaje = apiService.delVehiculo(vehiculo.placas).execute()
                    if(mensaje.isSuccessful) {
                        if (mensaje.body() != null) {
                            this.launch(Dispatchers.Main) {
                                cargarVehiculos()
                                alert(mensaje.body()!!.mensaje) { okButton { } }.show()
                            }
                        }
                    } else {
                        val erroMensaje = Gson().fromJson(mensaje.errorBody()!!.string(), Mensaje::class.java)
                        this.launch(Dispatchers.Main) { alert("${erroMensaje.mensaje} \n ${erroMensaje.error}") { okButton {  } }.show() }
                    }
                }
            }
            noButton { }
        }.show()
    }

    fun nuevoVehiculo(v: View) {
        startActivityForResult(intentFor<VehiculoActivity>("conductor" to conductor), 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            cargarVehiculos()
        }
    }
}
