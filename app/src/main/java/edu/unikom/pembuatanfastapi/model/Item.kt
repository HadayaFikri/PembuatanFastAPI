package edu.unikom.pembuatanfastapi.model

data class Item(
    val id: Int,
    val name: String,
    val description: String?, // Tanda '?' menunjukkan bahwa properti ini bisa null (Optional di FastAPI)
    val price: Double
)