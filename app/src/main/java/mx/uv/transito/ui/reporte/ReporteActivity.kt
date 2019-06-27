package mx.uv.transito.ui.reporte

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reporte.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.*
import mx.uv.transito.ui.foto.FotoActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*

class ReporteActivity : AppCompatActivity() {

    lateinit var conductor: Conductor
    lateinit var apiService: ApiService
    private var reporte: Reporte? = null
    private lateinit var location: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte)
        location = LocationServices.getFusedLocationProviderClient(this)
        conductor = intent.extras!!.getSerializable("conductor") as Conductor
        apiService = ApiService()
        spin_aseguradora.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    txt_poliza.isEnabled = false
                    txt_poliza.setText("")
                } else {
                    txt_poliza.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        solicitarGPS()
        cargarAseguradoras()
    }

    private fun solicitarGPS() {
        if (checkSelfPermission(this@ReporteActivity, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ReporteActivity, arrayOf(ACCESS_FINE_LOCATION), 0)
        }
        location.lastLocation.addOnSuccessListener { location ->
            txt_latitud.setText(location.latitude.toString())
            txt_longitud.setText(location.longitude.toString())
        }
        location.lastLocation.addOnFailureListener { exception ->
            GlobalScope.launch(Dispatchers.Main) { alert(exception.localizedMessage) { yesButton { finish() } } }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            alert("Se requiere el permiso de ubicación") { okButton { finish() } }
        }
    }

    private fun cargarAseguradoras() {
        GlobalScope.launch(Dispatchers.Default) {
            val aseguradoras = apiService.getAseguradoras().execute()
            if (aseguradoras.body() != null) {
                this.launch(Dispatchers.Main) {
                    val listaAseguradoras = aseguradoras.body() as ArrayList<Aseguradora>
                    listaAseguradoras.add(0, Aseguradora(null, "Ninguna"))
                    spin_aseguradora.adapter = ArrayAdapter<Aseguradora>(
                        applicationContext,
                        android.R.layout.simple_list_item_1,
                        listaAseguradoras
                    )
                    cargarVehiculos()
                }
            }
        }
    }

    private fun cargarVehiculos() {
        GlobalScope.launch(Dispatchers.Default) {
            val vehiculos = apiService.getVehiculosConductor(conductor.idConductor).execute()
            if (vehiculos.body() != null) {
                val listaVehiculos = vehiculos.body() as List<Vehiculo>
                this.launch(Dispatchers.Main) {
                    if(listaVehiculos.isNullOrEmpty()) {
                        alert("No hay vehículos registrados") { yesButton { finish() }}.show()
                    } else {
                        spin_vehiculo.adapter = ArrayAdapter<Vehiculo>(
                            applicationContext,
                            android.R.layout.simple_list_item_1,
                            listaVehiculos
                        )
                        cargarReporte()
                    }
                }
            }
        }
    }

    private fun cargarReporte() {
        reporte = intent?.extras?.get("reporte") as Reporte?
        if (reporte != null) {
            GlobalScope.launch(Dispatchers.Default) {
                val getImplicado = apiService.getImplicado(reporte!!.idImplicado).execute()
                if (getImplicado.body() != null) {
                    val implicado = getImplicado.body() as Implicado
                    this.launch(Dispatchers.Main) {
                        inicializarReporte(reporte!!, implicado)
                    }
                } else {
                    this.launch(Dispatchers.Main) {
                        val mensaje = Gson().fromJson(getImplicado.errorBody()!!.string(), Mensaje::class.java)
                        alert(mensaje.mensaje) { yesButton { finish() } }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun inicializarReporte(reporte: Reporte, implicado: Implicado) {
        btn_cancelar.text = "Cerrar"
        btn_guardar.isEnabled = false
        spin_vehiculo.isEnabled = false
        spin_vehiculo.visibility = View.GONE
        txt_vehiculo.text = reporte.placas
        txt_vehiculo.visibility = View.VISIBLE
        txt_placas.isEnabled = false
        txt_placas.setText(reporte.placas)
        txt_latitud.setText(reporte.latitud.toString())
        txt_longitud.setText(reporte.longitud.toString())
        txt_nombre.isEnabled = false
        txt_nombre.setText(implicado.nombre)
        txt_marca.isEnabled = false
        txt_marca.setText(implicado.marca)
        txt_modelo.isEnabled = false
        txt_modelo.setText(implicado.modelo)
        txt_color.isEnabled = false
        txt_color.setText(implicado.color)
        txt_fechaHora.setText(
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss",
                Locale.getDefault()
            ).format(reporte.fechaHora)
        )
        txt_fechaHora.visibility = View.VISIBLE
        spin_aseguradora.isEnabled = false
        txt_poliza.isEnabled = false
        btn_fotos.visibility = View.VISIBLE
        btn_dictamen.visibility = View.VISIBLE
        if (implicado.idAseguradora != null) {
            spin_aseguradora.setSelection(implicado.idAseguradora!!, false)
            txt_poliza.setText(implicado.poliza)
            return
        }
        spin_aseguradora.setSelection(Adapter.NO_SELECTION, false)
    }

    fun cancelar(v: View) {
        setResult(RESULT_CANCELED)
        finish()
    }

    fun guardar(v: View) {
        if (validar()) {
            val aseguradora = spin_aseguradora.selectedItem as Aseguradora
            val vehiculo = spin_vehiculo.selectedItem as Vehiculo
            val reporte = Reporte(
                idConductor = conductor.idConductor,
                idImplicado = -1,
                placas = vehiculo.placas,
                latitud = txt_latitud.text.toString().toDouble(),
                longitud = txt_longitud.text.toString().toDouble()
            )
            val implicado = Implicado(
                nombre = txt_nombre.text.toString().trim(),
                placas = txt_placas.text.toString().trim(),
                marca = txt_marca.text.toString().trim(),
                modelo = txt_modelo.text.toString().trim(),
                color = txt_color.text.toString().trim()
            )
            if (aseguradora.idAseguradora != null) {
                implicado.idAseguradora = (spin_aseguradora.selectedItem as Aseguradora).idAseguradora
                implicado.poliza = txt_poliza.text.toString().trim()
            }
            guardarImplicado(implicado, reporte)
        }
    }

    private fun guardarImplicado(implicado: Implicado, reporte: Reporte) {
        GlobalScope.launch(Dispatchers.Default) {
            val postImplicado = apiService.postImplicado(implicado).execute()
            if (postImplicado.body() != null) {
                this.launch(Dispatchers.Main) {
                    val newImplicado = postImplicado.body() as Implicado
                    reporte.idImplicado = newImplicado.idImplicado!!
                    guardarReporte(reporte)
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(postImplicado.errorBody()!!.string(), Mensaje::class.java)
                    alert(mensaje.mensaje) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    private fun guardarReporte(reporte: Reporte) {
        GlobalScope.launch(Dispatchers.Default) {
            val postReporte = apiService.postReporte(reporte).execute()
            if (postReporte.body() != null) {
                this.launch(Dispatchers.Main) {
                    alert("Reporte guardado correctamente") {
                        yesButton {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }.show()
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(postReporte.errorBody()!!.string(), Mensaje::class.java)
                    alert(mensaje.mensaje) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    private fun validar(): Boolean {
        val vehiculo = spin_vehiculo.selectedItem as Vehiculo?
        if (vehiculo == null) {
            alert("Debes seleccionar un vehículo") { yesButton {  } }
            return false
        }
        if (txt_latitud.text.isNullOrBlank()) {
            txt_latitud.error = "Espera a que se obtengan la latitud por GPS"
            txt_latitud.requestFocus()
            return false
        }
        if (txt_longitud.text.isNullOrBlank()) {
            txt_longitud.error = "Espera a que se obtengan la longitud por GPS"
            txt_longitud.requestFocus()
            return false
        }
        if (txt_placas.text.isNullOrBlank()) {
            txt_placas.error = "Debes introducir las placas del vehículo implicado"
            txt_placas.requestFocus()
            return false
        }
        if (txt_nombre.text.isNullOrBlank()) {
            txt_nombre.error = "Debes introducir el nombre del dueño del vehículo implicado"
            txt_nombre.requestFocus()
            return false
        }
        if (txt_marca.text.isNullOrBlank()) {
            txt_marca.error = "Debes introducir la marca del vehículo implicado"
            txt_marca.requestFocus()
            return false
        }
        if (txt_modelo.text.isNullOrBlank()) {
            txt_modelo.error = "Debes introducir el modelo del vehículo implicado"
            txt_modelo.requestFocus()
            return false
        }
        if (txt_color.text.isNullOrBlank()) {
            txt_color.error = "Debes introducir el color del vehículo implicado"
            txt_color.requestFocus()
            return false
        }
        val aseguradora = spin_aseguradora.selectedItem as Aseguradora?
        if (aseguradora?.idAseguradora != null) {
            if (txt_poliza.text.isNullOrBlank()) {
                txt_poliza.error = "Debes introducir la póliza del vehículo implicado"
                txt_poliza.requestFocus()
                return false
            }
        }
        return true
    }

    fun subirFotos(v: View) {
        startActivity(intentFor<FotoActivity>("reporte" to reporte))
    }

    fun verDictamen(v: View) {
        if(reporte!!.idDictamen == null) {
            alert("El reporte aun no se encuentra dictaminado") {
                yesButton {  }
            }.show()
        } else {
            GlobalScope.launch(Dispatchers.Default) {
                val getDictamen = apiService.getDictamen(reporte!!.idDictamen!!).execute()
                if(getDictamen.body() != null) {
                    val dictamen = getDictamen.body() as Dictamen
                    val getPersonal = apiService.getPersonal(dictamen.idPersonal).execute()
                    if(getPersonal.body() != null) {
                        this.launch(Dispatchers.Main) {
                            val personal = getPersonal.body() as Personal
                            alert("Folio ${dictamen.idDictamen} - ${dictamen.descripcion}",
                                "Dictaminado el ${SimpleDateFormat(
                                    "dd/MM/yyyy 'a las' HH:mm:ss",
                                    Locale.getDefault()
                                ).format(dictamen.fechaHora)} por ${personal.nombre}"
                            ) {
                                yesButton { }
                            }.show()
                        }
                    }
                }
            }
        }
    }
}
