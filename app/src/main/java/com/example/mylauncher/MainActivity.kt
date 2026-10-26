package com.example.mylauncher

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppItem(val label: String, val pkg: String, val icon: Drawable, val intent: Intent)

class MainActivity : Activity() {

    private lateinit var grid: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grid = findViewById(R.id.grid)
        grid.layoutManager = GridLayoutManager(this, 6) // কলাম সংখ্যা এখানে বদলান
    }

    override fun onResume() {
        super.onResume()
        grid.adapter = AppAdapter(loadApps()) { startActivity(it) }
    }

    // হোম স্ক্রিনে থাকা অবস্থায় Back চাপলে কিছু হবে না
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        val map = linkedMapOf<String, AppItem>()
        listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER).forEach { cat ->
            val query = Intent(Intent.ACTION_MAIN).addCategory(cat)
            pm.queryIntentActivities(query, 0).forEach { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg != packageName && !map.containsKey(pkg)) {
                    val launch = Intent(Intent.ACTION_MAIN)
                        .addCategory(cat)
                        .setClassName(pkg, ri.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    map[pkg] = AppItem(ri.loadLabel(pm).toString(), pkg, ri.loadIcon(pm), launch)
                }
            }
        }
        return map.values.sortedBy { it.label.lowercase() }
    }
}

class AppAdapter(
    private val items: List<AppItem>,
    private val onClick: (Intent) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val label: TextView = v.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.icon.setImageDrawable(item.icon)
        holder.label.text = item.label
        holder.itemView.setOnClickListener { onClick(item.intent) }
    }

    override fun getItemCount() = items.size
}
