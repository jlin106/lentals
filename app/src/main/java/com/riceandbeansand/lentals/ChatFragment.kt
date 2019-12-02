package com.riceandbeansand.lentals

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.w3c.dom.Text


class ChatFragment: Fragment() {

    var currentUserID : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val chatID = arguments!!.getString("chatID")!!
        val db = FirebaseFirestore.getInstance()
        var query = db.collection("chats").document(chatID).collection("messages").orderBy("timestamp")
        (activity as AppCompatActivity).supportActionBar!!.title = arguments?.getString("name", "UNKNOWN")

        val view = inflater.inflate(R.layout.chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.chat_area)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true //doesn't seem to be working on keyboard popup
        recyclerView.layoutManager = layoutManager

        val options = FirestoreRecyclerOptions.Builder<ChatItemSchema>()
                .setQuery(query, ChatItemSchema::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirestoreRecyclerAdapter<ChatItemSchema, ChatFragment.ViewHolder>(options) {
            override fun onDataChanged() {
                super.onDataChanged()
                recyclerView.scrollToPosition(this.getItemCount() - 1)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFragment.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.chat_bubble, parent, false)
                @Suppress
                //view.setClipToOutline(true)
                return ViewHolder(view)
            }

            // Views are recycled, so a view that was once a sent message might be rebound as a recieved message => must reset color
            override fun onBindViewHolder(holder: ChatFragment.ViewHolder, position: Int, model: ChatItemSchema) {
                holder.view.findViewById<TextView>(R.id.messageBubbleText).text = model.message
                var alignment = 0.0f
                var background = R.drawable.incoming_chat_rounded_rectangle
                var textColor = R.color.colorAccent
                if (model.senderID == currentUserID) {
                    alignment = 1.0f
                    background = R.drawable.outgoing_chat_rounded_rectangle
                    textColor = Color.WHITE
                }
                (holder.view.findViewById<View>(R.id.dummyView).layoutParams as LinearLayout.LayoutParams).weight = alignment
                holder.view.findViewById<TextView>(R.id.messageBubbleText).setBackgroundResource(background)
                holder.view.findViewById<TextView>(R.id.messageBubbleText).setTextColor(textColor)
            }
        }

        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.sendButton).setOnClickListener(View.OnClickListener {
            val messageTextView = view.findViewById<EditText>(R.id.messageText)
            val message = messageTextView.text.toString()
            messageTextView.setText("")
            val currentTime = System.currentTimeMillis()

            val docData = hashMapOf(
                    "message" to message,
                    "timestamp" to currentTime,
                    "senderID" to currentUserID
            )

            db.collection("chats").document(chatID).collection("messages")
                    .document().set(docData)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        Log.w("App", "Error writing chat msg", e)
                        messageTextView.setText(message)
                    }
        })

        return view
    }

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }
}
