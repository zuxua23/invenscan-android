package com.invenscan.app.ui.stockprep.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.R
import com.invenscan.app.databinding.ItemStockPrepDetailBinding
import com.invenscan.app.ui.stockprep.detail.PickItem

class StockPrepDetailAdapter : ListAdapter<PickItem, StockPrepDetailAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemStockPrepDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickItem) {
            binding.tvItemCode.text = item.detail.itemCode
            binding.tvItemName.text = item.detail.itemName
            binding.tvLocation.text = item.detail.locationName ?: "-"
            binding.tvRequestedQty.text = binding.root.context.getString(
                R.string.label_requested_qty, item.detail.requestedQty
            )
            binding.tvPickedQty.text = binding.root.context.getString(
                R.string.label_picked_qty, item.pickedQty
            )

            val isFulfilled = item.pickedQty >= item.detail.requestedQty
            val cardColor = if (isFulfilled) R.color.colorFoundBg else R.color.colorSurface
            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, cardColor)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockPrepDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PickItem>() {
        override fun areItemsTheSame(old: PickItem, new: PickItem) =
            old.detail.id == new.detail.id

        override fun areContentsTheSame(old: PickItem, new: PickItem) = old == new
    }
}
