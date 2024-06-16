package com.dicoding.capstone.dermaface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.capstone.dermaface.databinding.ActivityMainBinding
import com.dicoding.capstone.dermaface.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var cameraImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        articleViewModel = ViewModelProvider(this).get(ArticleViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // Setup RecyclerView Artikel
        binding.rvArticles.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        articleViewModel.articles.observe(this) { articles ->
            val articleAdapter = ArticleAdapter(this, articles)
            binding.rvArticles.adapter = articleAdapter
        }

        // Observasi perubahan data user
        userViewModel.user.observe(this) { user ->
            if (user == null) {
                // Jika tidak ada user (telah logout), kembali ke LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Observasi URI gambar
        mainViewModel.imageUri.observe(this) { uri ->
            uri?.let { startCrop(it) }
        }


        // Handle btn_scan click
        binding.btnScan.setOnClickListener {
            showImageSourceDialog()
        }

        // Setup ActivityResultLaunchers
        setupActivityResultLaunchers()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                // Arahkan ke HistoryActivity
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_logout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            val credentialManager = androidx.credentials.CredentialManager.create(this@MainActivity)
            Firebase.auth.signOut()
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galeri")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Pilih Sumber Gambar")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> galleryLauncher.launch("image/*")
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        try {
            cameraImageUri = getImageUri(this)
            cameraImageUri?.let { cameraLauncher.launch(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuat URI untuk gambar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let { uri ->
                    Log.d("MainActivity", "Captured Image URI: $uri") // Debug log
                    mainViewModel.setImageUri(uri)
                }
            } else {
                Log.e("MainActivity", "Failed to capture image")
                Toast.makeText(this, "Gagal menangkap gambar.", Toast.LENGTH_SHORT).show()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Log.d("MainActivity", "Gallery Image URI: $it") // Debug log
                mainViewModel.setImageUri(it)
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped.jpg"))
        Log.d("MainActivity", "Starting crop with URI: $sourceUri") // Debug log
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f) // Mengatur rasio aspek ke 1:1
                    .withMaxResultSize(1080, 1080) // Menyesuaikan ukuran maksimal jika diperlukan
                    .start(this@MainActivity)
            } catch (e: Exception) {
                Log.e("MainActivity", "UCrop error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Gagal memulai cropping.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data!!)
                if (resultUri != null) {
                    Log.d("MainActivity", "Cropped Image URI: $resultUri")
//                    mainViewModel.setImageUri(resultUri)// Debug log
                    // Lanjutkan ke ScanActivity dengan gambar yang sudah dipotong
                    val intent = Intent(this, ScanActivity::class.java)
                    intent.putExtra(ScanActivity.EXTRA_IMAGE_URI, resultUri.toString())
                    startActivity(intent)
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(data!!)
                cropError?.printStackTrace()
                Toast.makeText(this, "Gagal memotong gambar.", Toast.LENGTH_SHORT).show()
            }
        }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }
}