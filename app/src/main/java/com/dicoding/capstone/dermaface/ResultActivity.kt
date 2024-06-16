package com.dicoding.capstone.dermaface

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.databinding.ActivityResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var isDataSaved = false // Variabel untuk melacak status penyimpanan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        val diagnosis = intent.getStringExtra(EXTRA_DIAGNOSIS)
        val recommendation = intent.getStringExtra(EXTRA_RECOMMENDATION)

        // Debug log untuk URI
        Log.d(TAG, "Received Image URI: $imageUri") // Debug
        Log.d(TAG, "Received Diagnosis: $diagnosis") // Debug
        Log.d(TAG, "Received Recommendation: $recommendation") // Debug

        // Verifikasi bahwa URI valid dan load gambar baru
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.ivResult)
        }

        binding.tvDiagnosis.text = diagnosis
        binding.tvRecommendation.text = recommendation

        binding.btnSave.setOnClickListener {
            if (!isDataSaved) {
                saveDataToFirestore(imageUri, diagnosis, recommendation)
            } else {
                // Tampilkan pesan bahwa data sudah disimpan
                Toast.makeText(this, "Data sudah disimpan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDataToFirestore(imageUri: String?, diagnosis: String?, recommendation: String?) {
        if (imageUri == null || diagnosis == null || recommendation == null) return

        // Ambil UID pengguna
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Unggah gambar ke Firebase Storage
        val timestamp = System.currentTimeMillis()
        val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

        // Validasi URI sebelum unggahan
        val fileUri = Uri.parse(imageUri)
        if (fileUri == null) {
            Toast.makeText(this, "URI gambar tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        imageRef.putFile(fileUri)
            .addOnSuccessListener {
                // Dapatkan URL download
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userData = hashMapOf(
                        "uid" to uid,
                        "image_url" to uri.toString(),
                        "diagnosis" to diagnosis,
                        "recommendation" to recommendation,
                        "timestamp" to timestamp
                    )

                    // Simpan data ke Firestore di bawah dokumen pengguna
                    firestore.collection("user_data").document(uid)
                        .collection("scans")
                        .add(userData)
                        .addOnSuccessListener {
                            // Tampilkan pesan sukses dan nonaktifkan tombol Save
                            Toast.makeText(this, "Data berhasil disimpan.", Toast.LENGTH_SHORT).show()
                            isDataSaved = true // Update status penyimpanan
                        }
                        .addOnFailureListener {
                            // Tampilkan pesan error
                            Toast.makeText(this, "Gagal menyimpan data.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                // Tampilkan pesan error
                Toast.makeText(this, "Gagal mengupload gambar.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_DIAGNOSIS = "extra_diagnosis"
        const val EXTRA_RECOMMENDATION = "extra_recommendation"
    }
}
