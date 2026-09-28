package com.tvdrop.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tvdrop.app.R
import com.tvdrop.app.databinding.ItemTransferBinding
import com.tvdrop.app.model.TransferItem
import java.text.DecimalFormat

class TransferAdapter(
    private val items: MutableList<TransferItem>,
    private val onInstallApk: (TransferItem) -> Unit,
    private val onOpenFile: (TransferItem) -> Unit,
    private val onDeleteFile: (TransferItem) -> Unit,
    private val onActionFocused: () -> Unit
) : RecyclerView.Adapter<TransferAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTransferBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Let the action buttons own D-Pad focus; the card only mirrors their state.
            listOf(binding.btnInstall, binding.btnOpen, binding.btnDelete).forEach { button ->
                button.setOnFocusChangeListener { _, hasFocus ->
                    val cardFocused = hasFocus || binding.btnInstall.hasFocus() ||
                        binding.btnOpen.hasFocus() || binding.btnDelete.hasFocus()
                    itemView.isSelected = cardFocused
                    if (hasFocus) onActionFocused()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        val title = item.appName ?: item.fileName
        binding.tvTitle.text = title

        val sizeFormatted = formatFileSize(item.fileSize)
        val subtitle = if (item.isApk && !item.appVersion.isNullOrBlank()) {
            "${item.appVersion} • $sizeFormatted"
        } else {
            sizeFormatted
        }
        binding.tvSubtitle.text = subtitle
        binding.tvLocation.text = item.storageLabel
        holder.itemView.isSelected = binding.btnInstall.hasFocus() ||
            binding.btnOpen.hasFocus() || binding.btnDelete.hasFocus()

        // Set Icon
        if (item.appIcon != null) {
            binding.ivIcon.setImageDrawable(item.appIcon)
        } else {
            binding.ivIcon.setImageResource(
                if (item.isApk) R.mipmap.ic_launcher else android.R.drawable.ic_menu_save
            )
        }

        // Configure buttons depending on file type
        if (item.isApk) {
            binding.btnInstall.visibility = View.VISIBLE
            binding.btnOpen.visibility = View.GONE
            binding.btnInstall.setOnClickListener { onInstallApk(item) }
        } else {
            binding.btnInstall.visibility = View.GONE
            binding.btnOpen.visibility = View.VISIBLE
            binding.btnOpen.setOnClickListener { onOpenFile(item) }
        }

        binding.btnDelete.setOnClickListener { onDeleteFile(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TransferItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val format = DecimalFormat("#,##0.#")
        return "${format.format(sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
    }
}
