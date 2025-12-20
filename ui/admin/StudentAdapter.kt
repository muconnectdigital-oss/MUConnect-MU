package com.rsservice.muconnect.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.R
import java.util.*

class StudentAdapter(
    private val list: MutableList<Student>,
    private val onClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.VH>() {

    private val fullList = mutableListOf<Student>()

    init {
        fullList.addAll(list)
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_student, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = list[position]
        holder.tvName.text = s.fullName
        holder.tvInfo.text =
            "GR: ${s.grNo} | DOB: ${s.dob} | ${s. `class`}-${s.batch}"

        holder.itemView.setOnClickListener { onClick(s) }
    }

    fun update(newList: List<Student>) {
        list.clear()
        list.addAll(newList)
        fullList.clear()
        fullList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(className: String, batch: String) {
        list.clear()
        list.addAll(
            fullList.filter {
                (className == "All" || it. `class` == className) &&
                        (batch == "All" || it.batch == batch)
            }
        )
        notifyDataSetChanged()
    }
}
