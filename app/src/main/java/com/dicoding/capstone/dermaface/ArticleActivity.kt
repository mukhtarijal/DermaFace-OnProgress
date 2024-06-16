package com.dicoding.capstone.dermaface

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.capstone.dermaface.databinding.ActivityArticleBinding

class ArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dapatkan URL artikel dari Intent
        val articleUrl = intent.getStringExtra(EXTRA_ARTICLE_URL)

        // Setup WebView
        setupWebView(articleUrl ?: "")
    }

    private fun setupWebView(url: String) {
        binding.webView.apply {
            webViewClient = android.webkit.WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    }

    companion object {
        const val EXTRA_ARTICLE_URL = "com.dicoding.capstone.dermaface.EXTRA_ARTICLE_URL"
    }
}
