package com.rsservice.muconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.R
import com.rsservice.muconnect.model.User

class StudentsAdapter(
    private val list: MutableList<User>,
    private val onChecked: (User, Boolean) -> Unit
) : RecyclerView.Adapter<StudentsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_member, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = list[position]

        holder.tvName.text = user.name
        holder.tvRole.text = user.role

        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = false

        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            onChecked(user, isChecked)
        }
    }

    fun update(newList: List<User>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
