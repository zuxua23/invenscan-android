package com.invenscan.app.ui.stockout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.databinding.ItemStockOutBinding
import com.invenscan.app.ui.stockout.ScannedOutItem

class StockOutAdapter : ListAdapter<ScannedOutItem, StockOutAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemStockOutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScannedOutItem) {
            binding.tvItemName.text = if (item.isResolved) {
                item.itemName ?: binding.root.context.getString(
                    com.invenscan.app.R.string.label_unknown_item
                )
            } else {
                "Resolving…"
            }
            binding.tvItemCode.text = item.itemCode ?: item.scannedCode
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockOutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ScannedOutItem>() {
        override fun areItemsTheSame(old: ScannedOutItem, new: ScannedOutItem) =
            old.scannedCode == new.scannedCode

        override fun areContentsTheSame(old: ScannedOutItem, new: ScannedOutItem) = old == new
    }
}
