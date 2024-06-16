package com.dicoding.capstone.dermaface

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.databinding.ItemArticleBinding

class ArticleAdapter(private val context: Context, private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.binding.tvArticleTitle.text = article.title
        holder.binding.tvArticleDescription.text = article.description
        Glide.with(holder.itemView.context).load(article.image_url).into(holder.binding.ivArticleImage)

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ArticleActivity::class.java).apply {
                putExtra(ArticleActivity.EXTRA_ARTICLE_URL, article.link)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = articles.size

    class ArticleViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)
}
