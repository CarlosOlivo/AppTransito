package mx.uv.transito.ui.acceso

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_acceso.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Conductor
import mx.uv.transito.ui.menu.MenuActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton

class AccesoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso)
    }

    fun acceso(v: View) {
        if (txt_num.text.isNullOrBlank() || txt_num.text.toString().count() != 10) {
            alert("Introduce un numero de telefono valido") { yesButton { } }.show()
            return
        }
        if (txt_pass.text.isNullOrBlank()) {
            alert("Introduce una contraseña") { yesButton { } }.show()
            return
        }
        val apiService = ApiService()
        val acceso = HashMap<String, String>()
        GlobalScope.launch(Dispatchers.Default) {
            acceso["numCelular"] = txt_num.text.toString()
            acceso["contrasenia"] = txt_pass.text.toString()
            val conductor = apiService.postConductorAcceso(acceso).execute()
            if (conductor.body() != null) {
                this.launch(Dispatchers.Main) {
                    startActivity(intentFor<MenuActivity>("conductor" to conductor.body() as Conductor))
                }
            } else {
                this.launch(Dispatchers.Main) {
                    alert("Telefono y/o contraseña invalidos") {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    fun registro(v: View) {
        startActivity<RegistroActivity>()
    }
}
