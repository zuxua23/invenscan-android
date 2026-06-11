package com.invenscan.app.ui.stockin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.databinding.ItemScannedTagBinding
import com.invenscan.app.ui.stockin.ScannedItem

class ScannedItemAdapter : ListAdapter<ScannedItem, ScannedItemAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemScannedTagBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScannedItem) {
            binding.tvCode.text = item.scannedCode
            binding.tvItemName.text = item.itemName ?: "Resolving…"
            binding.tvScanType.text = item.scanType.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScannedTagBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ScannedItem>() {
        override fun areItemsTheSame(old: ScannedItem, new: ScannedItem) =
            old.scannedCode == new.scannedCode

        override fun areContentsTheSame(old: ScannedItem, new: ScannedItem) = old == new
    }
}
