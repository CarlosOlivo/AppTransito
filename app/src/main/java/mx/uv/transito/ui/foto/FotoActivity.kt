package mx.uv.transito.ui.foto

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.FileProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_foto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.uv.transito.R
import mx.uv.transito.data.ApiService
import mx.uv.transito.data.entity.Mensaje
import mx.uv.transito.data.entity.Reporte
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.alert
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.yesButton
import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class FotoActivity : AppCompatActivity() {
    private lateinit var reporte: Reporte
    private lateinit var photoURI: Uri
    private lateinit var foto: Bitmap
    lateinit var apiService: ApiService
    private val PATH = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES
    ).absolutePath.plus("${File.pathSeparator}transito")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto)

        reporte = intent.extras!!.getSerializable("reporte") as Reporte

        val opciones = ArrayList<String>()
        for (i in 0..7) {
            opciones.add("${reporte.placas}_${reporte.idReporte}_$i")
        }

        btn_guardar.isEnabled = false
        btn_ws.isEnabled = false
        if (!validarCamara()) {
            Toast.makeText(
                this, "El dispositivo no cuenta con camara",
                Toast.LENGTH_LONG
            ).show()
        }
        validarPermisosAlmacenamiento()
        apiService = ApiService()
    }

    private fun validarPermisosAlmacenamiento() {
        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            Toast.makeText(
                this, "SIN ACCESO AL ALMACENAMIENTO INTERNO",
                Toast.LENGTH_LONG
            ).show()
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun validarCamara(): Boolean {
        return packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_ANY
        )
    }

    fun tomarFoto(v: View) {
        btn_guardar.isEnabled = false
        btn_ws.isEnabled = false
        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Crear archivo temporal de imagen
        var tmp: File? = null
        try {
            tmp = crearArchivoTemporal()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        if (tmp != null) {
            // Acceder a la ruta completa del archivo
            photoURI = FileProvider.getUriForFile(
                this,
                "mx.uv.transito", tmp
            )
            i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            // Lanzar camara
            startActivityForResult(i, REQUEST_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun crearArchivoTemporal(): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val nombre = sdf.format(Date()) + ".jpg"
        val path = getExternalFilesDir(
            Environment.DIRECTORY_PICTURES + "/tmps"
        )
        return File.createTempFile(
            nombre,
            ".jpg", path
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        File(PATH).mkdirs()
        //--------DESDE CAMARA----------//
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                img_foto.setImageURI(this.photoURI)
                foto = (img_foto.drawable as BitmapDrawable).bitmap
                btn_guardar.isEnabled = true
                btn_ws.isEnabled = true
            }
        }
        //----------DESDE GALERIA----------//
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                this.photoURI = data!!.data!!
                img_foto.setImageURI(this.photoURI)
                foto = (img_foto.drawable as BitmapDrawable).bitmap
                btn_ws.isEnabled = true
            }
        }
    }

    fun abrirGaleria(v: View) {
        this.btn_guardar.isEnabled = false
        btn_ws.isEnabled = false
        val g = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
        startActivityForResult(g, PICK_IMAGE)
    }

    fun guardarFoto(v: View) {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val nombre = sdf.format(Date()) + ".jpg"
        val bitmap = foto
        val archivofinal = File(PATH, nombre)
        if (archivofinal.exists()) {
            archivofinal.delete()
        }
        try {
            val out = FileOutputStream(archivofinal)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            Toast.makeText(
                this, "Foto guardada correctamente",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /* Invoca el scanner de archivos, para que la nueva foto
           sea visible desde la galería del teléfono.
         */
        MediaScannerConnection.scanFile(this, arrayOf(archivofinal.toString()), null) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    fun subirAServidor(v: View) {
        val img = File(PATH,"tmp")
        val out = FileOutputStream(img)
        foto.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        GlobalScope.launch(Dispatchers.Default) {
            val postImg = apiService.newImg(reporte.idReporte!!, MultipartBody.Part.createFormData("img", "img", RequestBody.create(
                MediaType.parse("multipart/form-data"),img))).execute()
            if (postImg.body() != null) {
                reporte.imgs++
                this.launch(Dispatchers.Main) {
                    val newImage = postImg.body() as Mensaje
                    contentView!!.snackbar(newImage.mensaje)
                }
            } else {
                this.launch(Dispatchers.Main) {
                    val mensaje = Gson().fromJson(postImg.errorBody()!!.string(), Mensaje::class.java)
                    alert(mensaje.mensaje) {
                        yesButton { }
                    }.show()
                }
            }
        }
    }

    private fun Bitmap.convertToByteArray(): ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount

        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)

        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)

        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()

        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)

        //return bitmap's pixels
        return bytes
    }

    companion object {

        val REQUEST_CAPTURE = 1
        val PICK_IMAGE = 100
    }
}
