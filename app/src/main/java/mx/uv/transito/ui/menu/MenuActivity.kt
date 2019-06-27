package mx.uv.transito.ui.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import mx.uv.transito.R
import mx.uv.transito.data.entity.Conductor
import mx.uv.transito.ui.conductor.ConductorActivity
import mx.uv.transito.ui.reporte.ReportesActivity
import mx.uv.transito.ui.vehiculo.VehiculosActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class MenuActivity : AppCompatActivity() {

    private lateinit var conductor: Conductor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        conductor = intent.extras!!.getSerializable("conductor") as Conductor
        contentView!!.snackbar("Bienvenido ${conductor.nombre} ${conductor.apellidoPaterno}")
    }

    fun verPerfil(v: View) {
        startActivity(intentFor<ConductorActivity>("conductor" to conductor))
    }

    fun misVehiculos(v: View) {
        startActivity(intentFor<VehiculosActivity>("conductor" to conductor))
    }

    fun misReportes(v: View) {
        startActivity(intentFor<ReportesActivity>("conductor" to conductor))
    }

    fun salir(v: View) {
        finish()
    }
}
