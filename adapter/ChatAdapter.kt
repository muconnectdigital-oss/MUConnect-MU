package com.rsservice.muconnect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.databinding.ItemChatMessageBinding
import com.rsservice.muconnect.model.Message

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatVH>() {

    private val messages = mutableListOf<Message>()

    fun submitList(newList: List<Message>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ChatVH(val binding: ItemChatMessageBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatVH(binding)
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        holder.binding.tvMessage.text = messages[position].text
    }

    override fun getItemCount() = messages.size
}
