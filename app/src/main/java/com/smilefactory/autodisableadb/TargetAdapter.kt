package com.smilefactory.autodisableadb

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smilefactory.autodisableadb.databinding.ItemTargetBinding

class TargetAdapter(
    private val onRemove: (String) -> Unit,
) : RecyclerView.Adapter<TargetAdapter.Holder>() {

    private val items = mutableListOf<String>()

    fun submit(packages: Collection<String>) {
        items.clear()
        items.addAll(packages.sorted())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTargetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val pkg = items[position]
        val context = holder.itemView.context
        holder.binding.textPackage.text = pkg
        holder.binding.textAppName.text = InstalledApps.labelFor(context, pkg)
        holder.binding.imageIcon.setImageDrawable(
            InstalledApps.iconFor(context, pkg)
                ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher),
        )
        holder.binding.buttonRemove.setOnClickListener { onRemove(pkg) }
    }

    override fun getItemCount(): Int = items.size

    class Holder(val binding: ItemTargetBinding) : RecyclerView.ViewHolder(binding.root)
}
