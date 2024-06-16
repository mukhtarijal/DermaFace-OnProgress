package com.dicoding.capstone.dermaface

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("results")
    val articles: List<Article>
)

data class Article(
    val title: String,
    val description: String?,
    val image_url: String?,
    val link: String
)
