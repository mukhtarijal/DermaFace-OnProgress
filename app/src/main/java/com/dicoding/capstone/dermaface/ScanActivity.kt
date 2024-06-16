package com.dicoding.capstone.dermaface

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.dicoding.capstone.dermaface.databinding.ActivityScanBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        private const val INPUT_SIZE = 224
        private const val MODEL_PATH = "modelquantized.tflite"
        private const val TAG = "ScanActivity"
    }

    private lateinit var binding: ActivityScanBinding
    private lateinit var tflite: Interpreter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load TFLite model
        try {
            tflite = Interpreter(FileUtil.loadMappedFile(this, MODEL_PATH))
            Toast.makeText(this, "Model berhasil dimuat.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Model gagal dimuat: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Mendapatkan URI gambar dari Intent
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUri != null) {
            loadImage(Uri.parse(imageUri))
        } else {
            Toast.makeText(this, "Gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Handle tombol "Mulai"
        binding.btnScan.setOnClickListener {
            startScanning()
        }

        // Handle tombol "Batalkan"
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadImage(uri: Uri) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                runOnUiThread {
                    binding.imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun startScanning() {
        val bitmap = binding.imageView.drawable.toBitmap()
        analyzeImage(bitmap)
    }

    private fun analyzeImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
                val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

                val outputBuffer = Array(1) { FloatArray(10) }
                tflite.run(inputBuffer, outputBuffer)

                getMaxResult(outputBuffer[0])
            }

            val diagnosis = result.second
            val recommendation = getRecommendationFromFirestore(diagnosis)

            // Log untuk verifikasi
            Log.d(TAG, "Sending Image URI: ${intent.getStringExtra(EXTRA_IMAGE_URI)}")
            Log.d(TAG, "Sending Diagnosis: $diagnosis")
            Log.d(TAG, "Sending Recommendation: $recommendation")

            // Kirim data ke ResultActivity
            val intent = Intent(this@ScanActivity, ResultActivity::class.java).apply {
                putExtra(ResultActivity.EXTRA_IMAGE_URI, intent.getStringExtra(EXTRA_IMAGE_URI))
                putExtra(ResultActivity.EXTRA_DIAGNOSIS, diagnosis)
                putExtra(ResultActivity.EXTRA_RECOMMENDATION, recommendation)
            }
            startActivity(intent)
            finish()
        }
    }




    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((value and 0xFF) / 255.0f))
            }
        }
        return byteBuffer
    }

    private fun getMaxResult(confidences: FloatArray): Pair<Float, String> {
        val classLabels = arrayOf(
            "Actinic Keratosis", "Herpes", "Jerawat",
            "Kerutan", "Kulit Normal", "Mata Panda", "Milia",
            "Panu", "Rosacea", "Vitiligo"
        )

        var maxConfidence = Float.MIN_VALUE
        var maxIndex = -1
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxIndex = i
            }
        }
        return maxConfidence to classLabels[maxIndex]
    }

    private suspend fun getRecommendationFromFirestore(diagnosis: String): String {
        return try {
            val document = firestore.collection("recommendations")
                .document(diagnosis) // Gunakan diagnosis asli
                .get()
                .await()

            val tips = document.get("tips") as? List<String>
            tips?.joinToString("\n") ?: "Rekomendasi tidak ditemukan."
        } catch (e: Exception) {
            e.printStackTrace()
            "Gagal mengambil rekomendasi dari database."
        }
    }
}
