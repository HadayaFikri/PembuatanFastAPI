package edu.unikom.pembuatanfastapi.api

import edu.unikom.pembuatanfastapi.model.Item // Import model Item yang sudah dibuat
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Endpoint untuk mengambil semua item (GET /items/)
    @GET("items/")
    fun getItems(): Call<List<Item>>

    // Endpoint untuk membuat item baru (POST /items/)
    // @Body menandakan bahwa objek Item akan dikirim sebagai body request dalam format JSON
    @POST("items/")
    fun createItem(@Body item: Item): Call<Item>
}