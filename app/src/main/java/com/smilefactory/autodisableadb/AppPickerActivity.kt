package com.smilefactory.autodisableadb

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smilefactory.autodisableadb.databinding.ActivityAppPickerBinding
import com.smilefactory.autodisableadb.databinding.ItemAppBinding

class AppPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppPickerBinding
    private lateinit var adapter: AppAdapter
    private var allApps: List<InstalledApp> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UiInsets.apply(binding.root, binding.appBar, binding.recyclerApps)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AppAdapter { app ->
            setResult(RESULT_OK, Intent().putExtra(EXTRA_PACKAGE, app.packageName))
            finish()
        }
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        allApps = InstalledApps.launchable(packageManager)
            .filter { it.packageName != packageName }
        adapter.submit(allApps)
        updateEmpty()

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty().lowercase()
                val filtered = if (query.isBlank()) {
                    allApps
                } else {
                    allApps.filter {
                        it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
                    }
                }
                adapter.submit(filtered)
                updateEmpty()
            }
        })
    }

    private fun updateEmpty() {
        val empty = adapter.itemCount == 0
        binding.textEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.recyclerApps.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private class AppAdapter(
        private val onClick: (InstalledApp) -> Unit,
    ) : RecyclerView.Adapter<AppAdapter.Holder>() {

        private val items = mutableListOf<InstalledApp>()

        fun submit(apps: List<InstalledApp>) {
            items.clear()
            items.addAll(apps)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return Holder(binding)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val app = items[position]
            holder.binding.textAppName.text = app.label
            holder.binding.textPackage.text = app.packageName
            holder.binding.imageIcon.setImageDrawable(app.icon)
            holder.itemView.setOnClickListener { onClick(app) }
        }

        override fun getItemCount(): Int = items.size

        class Holder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
    }

    companion object {
        const val EXTRA_PACKAGE = "package_name"
    }
}
