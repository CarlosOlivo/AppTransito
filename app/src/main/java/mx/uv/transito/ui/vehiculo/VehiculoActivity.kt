package mx.uv.transito.ui.vehiculo

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_vehiculo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Aseguradora
import mx.uv.transito.data.entity.Conductor
import mx.uv.transito.data.entity.Mensaje
import mx.uv.transito.data.entity.Vehiculo
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.util.*

class VehiculoActivity : AppCompatActivity() {

    private lateinit var conductor: Conductor
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehiculo)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    txt_poliza.isEnabled = false
                    txt_poliza.text?.clear()
                } else {
                    txt_poliza.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        conductor = intent.extras!!.get("conductor") as Conductor
        apiService = ApiService()
        cargarAseguradoras()
    }

    private fun cargarAseguradoras() {
        GlobalScope.launch(Dispatchers.Default) {
            val aseguradoras = apiService.getAseguradoras().execute()
            if (aseguradoras.body() != null) {
                this.launch(Dispatchers.Main) {
                    val listaAseguradoras = aseguradoras.body() as ArrayList<Aseguradora>
                    listaAseguradoras.add(0, Aseguradora(null, "Ninguna"))
                    spinner.adapter = ArrayAdapter<Aseguradora>(
                        applicationContext,
                        android.R.layout.simple_list_item_1,
                        listaAseguradoras
                    )
                    cargarVehiculo()
                }
            }
        }
    }

    private fun cargarVehiculo() {
        val vehiculo = intent?.extras?.get("vehiculo") as Vehiculo?
        if(vehiculo != null) {
            txt_placas.isEnabled = false
            txt_placas.setText(vehiculo.placas)
            txt_marca.setText(vehiculo.marca)
            txt_modelo.setText(vehiculo.modelo)
            txt_anio.setText(vehiculo.anio.toString())
            txt_color.setText(vehiculo.color)
            if (vehiculo.idAseguradora != null) {
                spinner.setSelection(vehiculo.idAseguradora!!, false)
                txt_poliza.setText(vehiculo.poliza)
                return
            }
            spinner.setSelection(Adapter.NO_SELECTION, false)
        }
    }

    fun cancelar(v: View) {
        setResult(RESULT_CANCELED)
        finish()
    }

    fun guardar(v: View) {
        if (validar()) {
            val aseguradora = spinner.selectedItem as Aseguradora
            val vehiculo = Vehiculo(
                placas = txt_placas.text.toString().trim(),
                idConductor = conductor.idConductor,
                marca = txt_marca.text.toString().trim(),
                modelo = txt_modelo.text.toString().trim(),
                anio = txt_anio.text.toString().trim().toInt(),
                color = txt_color.text.toString().trim())
            if (aseguradora.idAseguradora != null) {
                vehiculo.idAseguradora = aseguradora.idAseguradora
                vehiculo.poliza = txt_poliza.text.toString().trim()
            }
            if(txt_placas.isEnabled) {
                guardarNuevoVehiculo(vehiculo)
            } else {
                guardarEdicionVehiculo(vehiculo)
            }
        }
    }

    private fun guardarNuevoVehiculo(newVehiculo: Vehiculo) {
        GlobalScope.launch(Dispatchers.Default) {
            val postVehiculo = apiService.postVehiculo(newVehiculo).execute()
            if (postVehiculo.body() != null) {
                this.launch(Dispatchers.Main) {
                    alert("Vehiculo guardado correctamente") {
                        yesButton {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }.show()
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(postVehiculo.errorBody()!!.string(), Mensaje::class.java)
                    alert(mensaje.mensaje) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    private fun guardarEdicionVehiculo(oldVehiculo: Vehiculo) {
        GlobalScope.launch(Dispatchers.Default) {
            val postVehiculo = apiService.putVehiculo(oldVehiculo.placas, oldVehiculo).execute()
            if (postVehiculo.body() != null) {
                this.launch(Dispatchers.Main) {
                    alert("Vehiculo editado correctamente") {
                        yesButton {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }.show()
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(postVehiculo.errorBody()!!.string(), Mensaje::class.java)
                    alert(mensaje.mensaje) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    private fun validar(): Boolean {
        if (txt_placas.text.isNullOrBlank()) {
            txt_placas.error = "Debes introducir las placas de tu vehículo"
            txt_placas.requestFocus()
            return false
        }
        if (txt_marca.text.isNullOrBlank()) {
            txt_marca.error = "Debes introducir la marca de tu vehículo"
            txt_marca.requestFocus()
            return false
        }
        if (txt_modelo.text.isNullOrBlank()) {
            txt_modelo.error = "Debes introducir el modelo de tu vehículo"
            txt_modelo.requestFocus()
            return false
        }
        if (txt_anio.text.isNullOrBlank() || !esNumero(txt_anio.text.toString()) ||
            txt_anio.text.toString().trim().length != 4
        ) {
            txt_anio.error = "Debes introducir el año de tu vehículo"
            txt_anio.requestFocus()
            return false
        }
        if (txt_color.text.isNullOrBlank()) {
            txt_color.error = "Debes introducir el color de tu vehículo"
            txt_color.requestFocus()
            return false
        }
        val aseguradora = spinner.selectedItem as Aseguradora
        if (aseguradora.idAseguradora != null) {
            if (txt_poliza.text.isNullOrBlank()) {
                txt_poliza.error = "Debes introducir la póliza de tu vehículo si seleccionas una aseguradora"
                txt_poliza.requestFocus()
                return false
            }
        }
        return true
    }

    private fun esNumero(numero: String): Boolean {
        return try {
            Integer.parseInt(numero)
            true
        } catch (e: NumberFormatException) {
            false
        }

    }
}
