package io.github.domi04151309.home.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.github.domi04151309.home.R
import android.view.LayoutInflater
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import io.github.domi04151309.home.data.SceneGridItem
import io.github.domi04151309.home.interfaces.RecyclerViewHelperInterface

class HueSceneGridAdapter(
    private val contextMenuListener: View.OnCreateContextMenuListener,
    private val helperInterface: RecyclerViewHelperInterface
    ) : RecyclerView.Adapter<HueSceneGridAdapter.ViewHolder>() {

    private var items: MutableList<SceneGridItem> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.grid_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.title.text = items[position].name
        holder.hidden.text = items[position].hidden
        if (items[position].color == null) {
            holder.drawable.setImageResource(R.drawable.ic_hue_scene_add)
        } else {
            val finalDrawable = LayerDrawable(arrayOf(
                ContextCompat.getDrawable(context, R.drawable.ic_hue_scene_base),
                ContextCompat.getDrawable(context, R.drawable.ic_hue_scene_color)
            ))
            finalDrawable.getDrawable(1).setTint(items[position].color ?: Color.WHITE)
            holder.drawable.setImageDrawable(finalDrawable)
        }
        holder.itemView.setOnClickListener { helperInterface.onItemClicked(holder.itemView, position) }
        holder.itemView.setOnCreateContextMenuListener(contextMenuListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: MutableList<SceneGridItem>) {
        if (newItems.size != items.size) {
            items = newItems
            notifyDataSetChanged()
        } else {
            val changed = arrayListOf<Int>()
            for (i in items.indices) {
                if (items[i] != newItems[i]) changed.add(i)
            }
            items = newItems
            changed.forEach(::notifyItemChanged)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drawable: ImageView = view.findViewById(R.id.drawable)
        val title: TextView = view.findViewById(R.id.title)
        val hidden: TextView = view.findViewById(R.id.hidden)
    }
}