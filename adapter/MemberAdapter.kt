package com.rsservice.muconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.R
import com.rsservice.muconnect.model.User

class MemberAdapter(
    private var list: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<MemberAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = list[position]

        holder.name.text = u.name
        holder.role.text = u.role
        holder.extra.text = u.extra ?: ""

        holder.itemView.setOnClickListener { onClick(u) }
    }

    override fun getItemCount(): Int = list.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvMemberName)
        val role: TextView = v.findViewById(R.id.tvMemberRole)
        val extra: TextView = v.findViewById(R.id.tvMemberExtra)
    }
}
