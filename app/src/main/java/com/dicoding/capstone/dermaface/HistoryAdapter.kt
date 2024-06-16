package com.dicoding.capstone.dermaface

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.capstone.dermaface.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val histories: List<HistoryResponse>,
    private val onItemClick: (HistoryResponse) -> Unit,
    private val onDeleteClick: (HistoryResponse) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = histories[position]

        // Format tanggal dan waktu
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = Date(history.timestamp)

        holder.binding.tvDate.text = dateFormat.format(date)
        holder.binding.tvDiagnosis.text = history.diagnosis

        // Debug log untuk memeriksa URL gambar
        val imageUrl = history.image_url
        if (imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
//                .placeholder(R.drawable.placeholder) // Tambahkan placeholder jika diperlukan
//                .error(R.drawable.error_image) // Tambahkan gambar error jika diperlukan
                .into(holder.binding.ivHistoryImage)

            // Debug log untuk URL gambar
            println("Loading image URL: $imageUrl")
        } else {
            Glide.with(holder.itemView.context)
                .clear(holder.binding.ivHistoryImage) // Bersihkan gambar jika URL kosong
        }

        holder.binding.root.setOnClickListener { onItemClick(history) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(history) }
    }

    override fun getItemCount() = histories.size
}
