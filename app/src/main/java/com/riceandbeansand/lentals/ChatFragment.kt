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
        view.findViewById<Button>(R.id.sendButton).setOnClickListener(View.OnClickListener {
            val messageTextView = view.findViewById<EditText>(R.id.messageText)
            val message = messageTextView.text.toString()
            Log.d("app", "message is " + message)
            val currentTime = System.currentTimeMillis()

            val docData = hashMapOf(
                    "message" to message,
                    "timestamp" to currentTime,
                    "senderID" to currentUserID
            )
            db.collection("chats").document(chatID).collection("messages")
                    .document().set(docData)
                    .addOnSuccessListener { messageTextView.setText("")}
                    .addOnFailureListener { e -> Log.w("App", "Error writing chat msg", e)  }
        })

        val options = FirestoreRecyclerOptions.Builder<ChatItemSchema>()
                .setQuery(query, ChatItemSchema::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirestoreRecyclerAdapter<ChatItemSchema, ChatFragment.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFragment.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.chat_bubble, parent, false)
                @Suppress
                //view.setClipToOutline(true)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatFragment.ViewHolder, position: Int, model: ChatItemSchema) {
                holder.view.findViewById<TextView>(R.id.messageBubbleText).text = model.message
                if (model.senderID == currentUserID) {
                    (holder.view.findViewById<View>(R.id.dummyView).layoutParams as LinearLayout.LayoutParams).weight = 1.0f
                    holder.view.findViewById<TextView>(R.id.messageBubbleText).setBackgroundResource(R.drawable.outgoing_chat_rounded_rectangle)
                    holder.view.findViewById<TextView>(R.id.messageBubbleText).setTextColor(Color.WHITE)
                }
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.chat_area)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }
}
