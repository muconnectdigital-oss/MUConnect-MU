package com.rsservice.muconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.R
import com.rsservice.muconnect.model.Group

class GroupsAdapter(
    private val list: MutableList<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvGroupName)
        val desc: TextView = v.findViewById(R.id.tvGroupDesc)
        val lastMsg: TextView = v.findViewById(R.id.tvLastMessage)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        return VH(LayoutInflater.from(p.context).inflate(R.layout.item_group, p, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val g = list[i]
        h.name.text = g.name
        h.desc.text = g.description
        h.lastMsg.text = g.lastMessage.ifEmpty { "No messages yet" }

        h.itemView.setOnClickListener { onClick(g) }
    }

    fun update(newList: List<Group>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
