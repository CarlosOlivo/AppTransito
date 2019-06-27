package mx.uv.transito.ui.reporte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_reportes.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Conductor
import mx.uv.transito.data.entity.Reporte
import org.jetbrains.anko.*

class ReportesActivity : AppCompatActivity() {

    lateinit var conductor: Conductor
    lateinit var apiService: ApiService
    lateinit var listaReportes: List<Reporte>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)
        conductor = intent.extras!!.get("conductor") as Conductor
        apiService = ApiService()
        lst_reportes.emptyView = empty
        lst_reportes.setOnItemClickListener { parent, view, position, id ->
            detalleReporte(position)
        }
        lst_reportes.setOnItemLongClickListener { parent, view, position, id ->
            eliminarReporte(position)
            return@setOnItemLongClickListener true
        }
        cargarReportes()
    }

    private fun cargarReportes() {
        GlobalScope.launch(Dispatchers.Default) {
            val reportes = apiService.getReportesConductor(conductor.idConductor).execute()
            if (reportes.body() != null) {
                listaReportes = reportes.body() as List<Reporte>
                this.launch(Dispatchers.Main) {
                    lst_reportes.adapter = ArrayAdapter<Reporte>(
                        this@ReportesActivity,
                        android.R.layout.simple_list_item_1,
                        listaReportes
                    )
                }
            }
        }
    }

    private fun detalleReporte(index: Int) {
        val reporte = listaReportes[index]
        startActivityForResult(intentFor<ReporteActivity>(
            "conductor" to conductor,
            "reporte" to reporte
        ), 0)
    }

    private fun eliminarReporte(index: Int) {
        val reporte = listaReportes[index]
        alert("¿Desea eliminar el reporte #${reporte.idReporte}?") {
            yesButton {
                GlobalScope.launch(Dispatchers.Default) {
                    val mensajeReporte = apiService.delReporte(reporte.idReporte!!).execute()
                    val mensajeImplicado = apiService.delImplicado(reporte.idImplicado).execute()
                    if(reporte.idDictamen != null) {
                        val mensajeDictamen = apiService.delDictamen(reporte.idDictamen!!).execute()
                    }
                    if(mensajeReporte.body() != null && mensajeImplicado.body() != null) {
                        this.launch(Dispatchers.Main) {
                            cargarReportes()
                            alert(mensajeReporte.body()!!.mensaje) { okButton { } }.show()
                        }
                    }
                }
            }
            noButton { }
        }.show()
    }

    fun nuevoReporte(v: View) {
        startActivityForResult(intentFor<ReporteActivity>(
            "conductor" to conductor
        ), 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            cargarReportes()
        }
    }
}
