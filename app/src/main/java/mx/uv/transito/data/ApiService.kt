package mx.uv.transito.data

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import mx.uv.transito.data.entity.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type

private const val API_URL = "http://192.168.137.1:8080/api/"

interface ApiService {
    @POST("conductor/acceso")
    fun postConductorAcceso(
        @Body body: HashMap<String, String>
    ): Call<Conductor>

    @POST("conductor")
    fun postConductorRegistro(
        @Body body: HashMap<String, String>
    ): Call<Conductor>

    @GET("vehiculo/conductor/{id}")
    fun getVehiculosConductor(
        @Path("id") id: Int
    ): Call<List<Vehiculo>>

    @GET("aseguradora")
    fun getAseguradoras(): Call<List<Aseguradora>>

    @POST("vehiculo")
    fun postVehiculo(
        @Body body: Vehiculo
    ): Call<Vehiculo>

    @PUT("vehiculo/{placas}")
    fun putVehiculo(
        @Path("placas") placas: String,
        @Body body: Vehiculo
    ): Call<Vehiculo>

    @DELETE("vehiculo/{placas}")
    fun delVehiculo(
        @Path("placas") placas: String
    ): Call<Mensaje>

    @GET("reporte/conductor/{id}")
    fun getReportesConductor(
        @Path("id") id: Int
    ): Call<List<Reporte>>

    @DELETE("reporte/{id}")
    fun delReporte(
        @Path("id") id: Int
    ): Call<Mensaje>

    @DELETE("implicado/{id}")
    fun delImplicado(
        @Path("id") id: Int
    ): Call<Mensaje>

    @GET("implicado/{id}")
    fun getImplicado(
        @Path("id") id: Int
    ): Call<Implicado>

    @POST("implicado")
    fun postImplicado(
        @Body body: Implicado
    ): Call<Implicado>

    @POST("reporte")
    fun postReporte(
        @Body body: Reporte
    ): Call<Reporte>

    @GET("dictamen/{id}")
    fun getDictamen(
        @Path("id") id: Int
    ): Call<Dictamen>

    @GET("personal/{id}")
    fun getPersonal(
        @Path("id") id: Int
    ): Call<Personal>

    @DELETE("dictamen/{id}")
    fun delDictamen(
        @Path("id") id: Int
    ): Call<Mensaje>

    @Multipart
    @POST("reporte/img")
    fun newImg(
        @Part("idReporte") idReporte: Int,
        @Part img: MultipartBody.Part
    ): Call<Mensaje>

    companion object {
        operator fun invoke(): ApiService {
            val gson = GsonBuilder()
                .setDateFormat("dd/MM/yyyy")
                .serializeNulls()
                .create()
            return Retrofit.Builder()
                .baseUrl(API_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(nullOnEmptyConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}

val nullOnEmptyConverterFactory = object : Converter.Factory() {
    fun converterFactory() = this
    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object : Converter<ResponseBody, Any?> {
        val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
        override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
    }
}