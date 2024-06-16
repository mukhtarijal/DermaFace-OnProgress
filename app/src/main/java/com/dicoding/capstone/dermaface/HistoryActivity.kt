package com.dicoding.capstone.dermaface

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.capstone.dermaface.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var historyAdapter: HistoryAdapter
    private val histories = mutableListOf<HistoryResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchScanHistories()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(histories,
            onItemClick = { history ->
                val intent = Intent(this, DetailHistoryActivity::class.java).apply {
                    putExtra("HISTORY_ID", history.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { history ->
                deleteScanHistory(history)
            })

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter
    }

    private fun fetchScanHistories() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("user_data").document(uid)
            .collection("scans")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                histories.clear()
                for (document in result) {
                    val history = document.toObject(HistoryResponse::class.java)
                    history.id = document.id

                    // Debug log untuk verifikasi data
                    Log.d("HistoryActivity", "Fetched history: $history")
                    histories.add(history)
                }
                historyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendapatkan riwayat.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteScanHistory(history: HistoryResponse) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("user_data").document(uid)
            .collection("scans").document(history.id)
            .delete()
            .addOnSuccessListener {
                histories.remove(history)
                historyAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Riwayat berhasil dihapus.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus riwayat.", Toast.LENGTH_SHORT).show()
            }
    }
}
