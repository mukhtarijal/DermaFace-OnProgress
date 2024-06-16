package com.dicoding.capstone.dermaface

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/1/news")
    fun getArticles(
        @Query("apikey") apiKey: String,
        @Query("q") query: String
    ): Call<NewsResponse>
}
