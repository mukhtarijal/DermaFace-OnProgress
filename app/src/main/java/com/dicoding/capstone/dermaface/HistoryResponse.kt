package com.dicoding.capstone.dermaface

data class HistoryResponse(
    var id: String = "", // Ubah menjadi var untuk memungkinkan perubahan
    val image_url: String = "",
    val diagnosis: String = "",
    val recommendation: String = "",
    val timestamp: Long = 0L // Pastikan timestamp adalah Long
)
