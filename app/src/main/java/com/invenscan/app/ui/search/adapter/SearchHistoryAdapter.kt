package com.invenscan.app.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.databinding.ItemSearchHistoryBinding
import com.invenscan.app.ui.search.SearchHistoryItem

class SearchHistoryAdapter : ListAdapter<SearchHistoryItem, SearchHistoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchHistoryItem) {
            binding.tvCode.text = item.code
            binding.tvItemName.text = item.result?.itemName ?: "Tidak ditemukan"
            binding.tvLocation.text = item.result?.locationName ?: "-"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SearchHistoryItem>() {
        override fun areItemsTheSame(old: SearchHistoryItem, new: SearchHistoryItem) =
            old.code == new.code && old.result?.id == new.result?.id

        override fun areContentsTheSame(old: SearchHistoryItem, new: SearchHistoryItem) = old == new
    }
}
