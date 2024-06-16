package com.dicoding.capstone.dermaface

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ArticleViewModel(private val repository: ArticleRepository) : ViewModel() {
    private val apiKey = "pub_456051edba1bb1293b245812bb5f877044551"
    private val query = "kulit wajah"

    val articles: LiveData<List<Article>> = repository.fetchArticles(apiKey, query)
}
