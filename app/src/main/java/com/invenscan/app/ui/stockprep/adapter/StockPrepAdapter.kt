package com.invenscan.app.ui.stockprep.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.invenscan.app.R
import com.invenscan.app.data.model.StockPrepModel
import com.invenscan.app.data.model.StockPrepStatus
import com.invenscan.app.databinding.ItemStockPrepBinding

class StockPrepAdapter(
    private val onItemClick: (StockPrepModel) -> Unit
) : ListAdapter<StockPrepModel, StockPrepAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemStockPrepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockPrepModel) {
            binding.tvDocNumber.text = item.docNumber
            binding.tvNotes.text = item.notes ?: "-"
            binding.tvCreatedAt.text = item.createdAt?.take(10) ?: "-"
            binding.tvStatus.text = item.status

            val (bgColor, textColor) = when (item.status) {
                StockPrepStatus.OPEN -> Pair(R.color.colorStatusOpenBg, R.color.colorStatusOpen)
                StockPrepStatus.IN_PROGRESS -> Pair(R.color.colorStatusInProgressBg, R.color.colorStatusInProgress)
                StockPrepStatus.DONE -> Pair(R.color.colorStatusDoneBg, R.color.colorStatusDone)
                else -> Pair(R.color.colorStatusClosedBg, R.color.colorStatusClosed)
            }

            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(binding.root.context, bgColor))
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, textColor))
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockPrepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StockPrepModel>() {
        override fun areItemsTheSame(old: StockPrepModel, new: StockPrepModel) = old.id == new.id
        override fun areContentsTheSame(old: StockPrepModel, new: StockPrepModel) = old == new
    }
}
