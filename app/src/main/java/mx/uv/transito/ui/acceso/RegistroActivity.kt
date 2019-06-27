package mx.uv.transito.ui.acceso

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_acceso.txt_num
import kotlinx.android.synthetic.main.activity_registro.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Mensaje
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import java.text.SimpleDateFormat
import java.util.*

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
    }

    fun registrar(v: View) {
        if(txt_nom.text.isNullOrBlank()) {
            alert("Debes introducir tu(s) nombre(s)") {
                okButton {  }
            }.show()
            return
        }
        if(txt_apeP.text.isNullOrBlank()) {
            alert("Debes introducir tu apellido paterno") {
                okButton {  }
            }.show()
            return
        }
        if(txt_fechaN.text.isNullOrBlank() || !esFecha(txt_fechaN.text.toString())) {
            alert("Debes introducir tu fecha de nacimiento en formato dd/MM/yyyy") {
                okButton {  }
            }.show()
            return
        }
        if(txt_lic.text.isNullOrBlank() || !esNumero(txt_lic.text.toString())) {
            alert("Debes introducir tu número de licencia de conducir") {
                okButton {  }
            }.show()
            return
        }
        if(txt_num.text.isNullOrBlank() || !esNumero(txt_num.text.toString()) || txt_num.text.toString().count() != 10) {
            alert("Debes introducir tu número celular de 10 dígitos") {
                okButton {  }
            }.show()
            return
        }
        if(txt_con.text.isNullOrBlank()) {
            alert("Debes introducir tu contraseña") {
                okButton {  }
            }.show()
            return
        }
        val apiService = ApiService()
        val registro = HashMap<String, String>()
        GlobalScope.launch(Dispatchers.Default) {
            registro["nombre"] = txt_nom.text.toString()
            registro["apellidoPaterno"] = txt_apeP.text.toString()
            registro["apellidoMaterno"] = txt_apeM.text.toString()
            registro["fechaNacimiento"] = txt_fechaN.text.toString()
            registro["numLicencia"] = txt_lic.text.toString()
            registro["numCelular"] = txt_num.text.toString()
            registro["contrasenia"] = txt_con.text.toString()
            val conductor = apiService.postConductorRegistro(registro).execute()
            if (conductor.body() != null) {
                this.launch(Dispatchers.Main) {
                    alert("Cuenta creada correctamente") {
                        yesButton { finish() }
                    }.show()
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(conductor.errorBody().toString(), Mensaje::class.java)
                    alert(mensaje.toString()) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    fun regresar(v: View) {
        finish()
    }

    private fun esFecha(fecha: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        return try {
            sdf.parse(fecha)
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun esNumero(numero: String): Boolean {
        return try {
            numero.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}
