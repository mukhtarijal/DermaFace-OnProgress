// DetailHistoryActivity.kt
package com.dicoding.capstone.dermaface

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.databinding.ActivityDetailHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val historyId = intent.getStringExtra("HISTORY_ID")
        if (historyId != null) {
            fetchHistoryDetails(historyId)
        } else {
            Toast.makeText(this, "Tidak ada data riwayat.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchHistoryDetails(historyId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("user_data").document(uid)
            .collection("scans").document(historyId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val history = document.toObject(HistoryResponse::class.java)
                    if (history != null) {
                        binding.tvDate.text = android.text.format.DateFormat.format("dd MMM yyyy", history.timestamp)
                        binding.tvDiagnosis.text = history.diagnosis
                        binding.tvRecommendation.text = history.recommendation
                        Glide.with(this).load(history.image_url).into(binding.ivHistory)
                    }
                } else {
                    Toast.makeText(this, "Data riwayat tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendapatkan detail riwayat.", Toast.LENGTH_SHORT).show()
            }
    }
}
