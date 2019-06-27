package mx.uv.transito.ui.conductor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_conductor.*
import mx.uv.transito.R
import mx.uv.transito.data.entity.Conductor
import java.text.SimpleDateFormat
import java.util.*

class ConductorActivity : AppCompatActivity() {

    lateinit var conductor: Conductor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conductor)
        conductor = intent.extras!!.get("conductor") as Conductor
        inicializar()
    }

    @SuppressLint("SetTextI18n")
    private fun inicializar() {
        nombre.text = "${conductor.nombre} ${conductor.apellidoPaterno} ${conductor.apellidoMaterno}"
        fechaN.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(conductor.fechaNacimiento)
        licencia.text = "${conductor.numLicencia}"
        numCelular.text = conductor.numCelular
    }

}
