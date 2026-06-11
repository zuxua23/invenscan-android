package com.invenscan.app.ui.stocktaking.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.R
import com.invenscan.app.databinding.ItemScanResultBinding
import com.invenscan.app.ui.stocktaking.detail.ScanResultItem
import com.invenscan.app.ui.stocktaking.detail.ScanResultStatus

class ScanResultAdapter : ListAdapter<ScanResultItem, ScanResultAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemScanResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanResultItem) {
            binding.tvItemCode.text = item.itemCode
            binding.tvItemName.text = item.itemName
            binding.tvTagId.text = item.tagId

            val (bgColor, textColor, statusLabel) = when (item.status) {
                ScanResultStatus.FOUND -> Triple(R.color.colorFoundBg, R.color.colorFound, "DITEMUKAN")
                ScanResultStatus.MISSING -> Triple(R.color.colorMissingBg, R.color.colorMissing, "HILANG")
                ScanResultStatus.UNKNOWN -> Triple(R.color.colorUnknownBg, R.color.colorUnknown, "TIDAK DIKENAL")
            }

            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, bgColor))
            binding.tvStatus.text = statusLabel
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, textColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ScanResultItem>() {
        override fun areItemsTheSame(old: ScanResultItem, new: ScanResultItem) =
            old.tagId == new.tagId

        override fun areContentsTheSame(old: ScanResultItem, new: ScanResultItem) = old == new
    }
}
