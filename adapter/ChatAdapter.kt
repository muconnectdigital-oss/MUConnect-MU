package com.rsservice.muconnect.adapter

import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rsservice.muconnect.R
import com.rsservice.muconnect.model.Message
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp

class ChatAdapter(
    private val list: MutableList<Message>,
    private val myId: String,
    var onReply: (Message) -> Unit = {},
    var onReact: (String, String) -> Unit = { _, _ -> },
    var onDelete: (String) -> Unit = {},
    var onEdit: (Message) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) =
        if (list[position].senderId == myId) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder =
        if (type == 1)
            RightVH(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_right, parent, false))
        else
            LeftVH(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_left, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val m = list[pos]
        val ctx = holder.itemView.context

        val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(m.createdAt?.toDate() ?: Date())
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(m.createdAt?.toDate() ?: Date())

        val showDate = pos == 0 || !isSameDay(list[pos - 1].createdAt, m.createdAt)

        when (holder) {
            is RightVH -> {
                holder.date.visibility = if (showDate) View.VISIBLE else View.GONE
                holder.date.text = date

                holder.sender.visibility = View.GONE
                holder.msg.text = m.text
                holder.time.text = time + getSeenIcon(m)

                setFileMedia(m, holder.media, holder.play, ctx)
            }

            is LeftVH -> {
                holder.date.visibility = if (showDate) View.VISIBLE else View.GONE
                holder.date.text = date

                holder.sender.visibility = View.VISIBLE
                holder.sender.text = m.senderName
                holder.msg.text = m.text
                holder.time.text = time

                setFileMedia(m, holder.media, holder.play, ctx)

                holder.itemView.setOnClickListener {
                    if (!m.fileUrl.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(m.fileUrl))
                        ctx.startActivity(intent)
                    }
                }
            }
        }

        // reply bubble
        when (holder) {
            is LeftVH -> setupReplyUI(holder.reply, m)
            is RightVH -> setupReplyUI(holder.reply, m)
        }

        // reaction UI
        when (holder) {
            is LeftVH -> setupReactionUI(holder.reaction, m)
            is RightVH -> setupReactionUI(holder.reaction, m)
        }

        // long press - reaction popup
        holder.itemView.setOnLongClickListener {
            showReactionPopup(holder.itemView) { emoji ->
                onReact(m.id, emoji)
            }
            true
        }

        // click msg to reply
        holder.itemView.setOnClickListener {
            onReply(m)
        }

        // long press for delete/edit
        holder.itemView.setOnLongClickListener {
            showMessageMenu(holder.itemView, m)
            true
        }
    }

    override fun getItemCount() = list.size

    fun update(newList: List<Message>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    private fun setupReplyUI(tv: TextView, m: Message) {
        if (!m.replyToText.isNullOrEmpty()) {
            tv.visibility = View.VISIBLE
            tv.text = "↪ ${m.replyToText}"
        } else tv.visibility = View.GONE
    }

    private fun setupReactionUI(tv: TextView, m: Message) {
        if (!m.reaction.isNullOrEmpty()) {
            tv.visibility = View.VISIBLE
            tv.text = m.reaction
        } else tv.visibility = View.GONE
    }

    private fun getSeenIcon(m: Message): String =
        if (m.seenBy != null && m.seenBy.size > 1) " ✔✔" else " ✔"

    private fun isSameDay(ts1: Timestamp?, ts2: Timestamp?): Boolean {
        if (ts1 == null || ts2 == null) return false
        val cal1 = Calendar.getInstance().apply { time = ts1.toDate() }
        val cal2 = Calendar.getInstance().apply { time = ts2.toDate() }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setFileMedia(m: Message, media: ImageView, play: ImageView, ctx: android.content.Context) {
        when (m.type) {
            "image" -> {
                media.visibility = View.VISIBLE
                Glide.with(ctx).load(m.fileUrl).into(media)
                play.visibility = View.GONE
            }
            "video" -> {
                media.visibility = View.VISIBLE
                play.visibility = View.VISIBLE
                Glide.with(ctx).load(m.fileUrl).into(media)
            }
            else -> {
                media.visibility = View.GONE
                play.visibility = View.GONE
            }
        }
    }

    private fun showReactionPopup(v: View, callback: (String) -> Unit) {
        val popup = PopupMenu(v.context, v)
        val emojis = listOf("👍","❤️","😂","🔥","😢","😡")
        emojis.forEachIndexed { i, emoji ->
            popup.menu.add(0, i, 0, emoji)
        }
        popup.setOnMenuItemClickListener {
            callback(it.title.toString())
            true
        }
        popup.show()
    }

    private fun showMessageMenu(v: View, m: Message) {
        val popup = PopupMenu(v.context, v)
        popup.menu.add("Reply")
        if (m.senderId == myId) {
            popup.menu.add("Edit")
            popup.menu.add("Delete")
        }
        popup.setOnMenuItemClickListener {
            when (it.title) {
                "Reply" -> onReply(m)
                "Edit" -> onEdit(m)
                "Delete" -> onDelete(m.id)
            }
            true
        }
        popup.show()
    }

    class LeftVH(v: View) : RecyclerView.ViewHolder(v) {
        val date: TextView = v.findViewById(R.id.tvDate)
        val sender: TextView = v.findViewById(R.id.tvSender)
        val msg: TextView = v.findViewById(R.id.tvMessage)
        val time: TextView = v.findViewById(R.id.tvTime)
        val media: ImageView = v.findViewById(R.id.ivMedia)
        val play: ImageView = v.findViewById(R.id.ivPlay)
        val reply: TextView = v.findViewById(R.id.tvReply)
        val reaction: TextView = v.findViewById(R.id.tvReaction)
    }

    class RightVH(v: View) : RecyclerView.ViewHolder(v) {
        val date: TextView = v.findViewById(R.id.tvDate)
        val sender: TextView = v.findViewById(R.id.tvSender)
        val msg: TextView = v.findViewById(R.id.tvMessage)
        val time: TextView = v.findViewById(R.id.tvTime)
        val media: ImageView = v.findViewById(R.id.ivMedia)
        val play: ImageView = v.findViewById(R.id.ivPlay)
        val reply: TextView = v.findViewById(R.id.tvReply)
        val reaction: TextView = v.findViewById(R.id.tvReaction)
    }
}
