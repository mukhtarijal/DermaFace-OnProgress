package com.dicoding.capstone.dermaface

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleRepository {

    fun fetchArticles(apiKey: String, query: String): LiveData<List<Article>> {
        val articlesLiveData = MutableLiveData<List<Article>>()
        ApiConfig.apiService.getArticles(apiKey, query).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                articlesLiveData.value = if (response.isSuccessful) {
                    response.body()?.articles?.filter { it.hasValidData() }.orEmpty()
                } else {
                    emptyList()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                articlesLiveData.value = emptyList()
            }
        })
        return articlesLiveData
    }

    private fun Article.hasValidData(): Boolean {
        return title.isNotEmpty() && link.isNotEmpty() && !image_url.isNullOrEmpty()
    }
}
