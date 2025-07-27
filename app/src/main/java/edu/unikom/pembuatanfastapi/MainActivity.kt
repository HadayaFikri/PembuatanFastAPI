package edu.unikom.pembuatanfastapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import edu.unikom.pembuatanfastapi.api.ApiService // Import ApiService Anda
import edu.unikom.pembuatanfastapi.model.Item // Import Item model Anda
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // Deklarasi variabel untuk elemen UI
    private lateinit var apiService: ApiService
    private lateinit var itemNameEditText: EditText
    private lateinit var itemDescEditText: EditText
    private lateinit var itemPriceEditText: EditText
    private lateinit var addButton: Button
    private lateinit var showAllButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Mengatur layout untuk activity ini

        // Inisialisasi Views dari layout
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemDescEditText = findViewById(R.id.itemDescEditText)
        itemPriceEditText = findViewById(R.id.itemPriceEditText)
        addButton = findViewById(R.id.addButton)
        showAllButton = findViewById(R.id.showAllButton)
        resultTextView = findViewById(R.id.resultTextView)

        // --- Konfigurasi Retrofit ---
        // Penting: Sesuaikan BASE_URL ini!
        // - Jika menggunakan emulator Android: localhost PC Anda adalah 10.0.2.2
        // - Jika menggunakan perangkat fisik: Ganti dengan IP Address lokal komputer Anda
        //   (contoh: "http://192.168.1.10:8000/", dapatkan IP Anda dari `ipconfig` / `ifconfig`)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // URL dasar API Anda
            .addConverterFactory(GsonConverterFactory.create()) // Converter untuk JSON
            .build()

        // Membuat instance dari ApiService
        apiService = retrofit.create(ApiService::class.java)

        // --- Event Listener untuk Tombol ---
        addButton.setOnClickListener {
            addItem() // Panggil fungsi saat tombol "Tambah Item" diklik
        }

        showAllButton.setOnClickListener {
            fetchAllItems() // Panggil fungsi saat tombol "Tampilkan Semua Item" diklik
        }
    }

    // Fungsi untuk menambahkan item baru ke API
    private fun addItem() {
        // Ambil teks dari EditText dan hapus spasi di awal/akhir
        val name = itemNameEditText.text.toString().trim()
        val description = itemDescEditText.text.toString().trim()
        val priceString = itemPriceEditText.text.toString().trim()
        val price = priceString.toDoubleOrNull() // Konversi harga ke Double, atau null jika input tidak valid

        // Validasi input sederhana
        if (name.isBlank() || price == null) {
            resultTextView.text = "Nama dan Harga tidak boleh kosong atau tidak valid!"
            return
        }

        // Buat objek Item. ID diisi 0 karena backend FastAPI akan meng-generate ID-nya.
        // Gunakan `description.ifBlank { null }` untuk mengirim null jika deskripsi kosong
        val newItem = Item(id = 0, name = name, description = description.ifBlank { null }, price = price)

        // Lakukan panggilan API asinkron untuk membuat item
        apiService.createItem(newItem).enqueue(object : Callback<Item> {
            // Dipanggil ketika ada respons dari server (baik sukses maupun error HTTP)
            override fun onResponse(call: Call<Item>, response: Response<Item>) {
                if (response.isSuccessful) {
                    val createdItem = response.body() // Ambil objek Item dari body respons
                    resultTextView.text = "Item ditambahkan: ${createdItem?.name} (ID: ${createdItem?.id})"
                    Log.d("API_CALL", "Item Added: $createdItem")
                    // Kosongkan input setelah berhasil menambahkan
                    itemNameEditText.text.clear()
                    itemDescEditText.text.clear()
                    itemPriceEditText.text.clear()
                } else {
                    // Jika respons tidak sukses (misalnya 400, 404, 500)
                    val errorBody = response.errorBody()?.string() // Ambil pesan error dari body respons
                    resultTextView.text = "Gagal menambahkan item: ${response.code()} - $errorBody"
                    Log.e("API_CALL", "Error adding item: ${response.code()} - $errorBody")
                }
            }

            // Dipanggil ketika ada kesalahan jaringan (misalnya tidak ada internet, server tidak dapat dijangkau)
            override fun onFailure(call: Call<Item>, t: Throwable) {
                resultTextView.text = "Kesalahan jaringan: ${t.message}"
                Log.e("API_CALL", "Network Error: ${t.message}", t)
            }
        })
    }

    // Fungsi untuk mengambil semua item dari API
    private fun fetchAllItems() {
        // Lakukan panggilan API asinkron untuk mengambil daftar item
        apiService.getItems().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    val items = response.body() // Ambil daftar Item dari body respons
                    if (items.isNullOrEmpty()) {
                        resultTextView.text = "Tidak ada data item yang tersedia."
                    } else {
                        // Format daftar item menjadi string untuk ditampilkan
                        val itemDetails = items.joinToString("\n") {
                            "${it.id}. ${it.name} - Rp.${"%.2f".format(it.price)}" +
                                    (it.description?.let { desc -> " (${desc})" } ?: "") // Tambahkan deskripsi jika ada
                        }
                        resultTextView.text = "Daftar Item:\n$itemDetails"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    resultTextView.text = "Gagal mengambil data: ${response.code()} - $errorBody"
                    Log.e("API_CALL", "Error fetching items: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                resultTextView.text = "Kesalahan jaringan: ${t.message}"
                Log.e("API_CALL", "Network Error: ${t.message}", t)
            }
        })
    }
}