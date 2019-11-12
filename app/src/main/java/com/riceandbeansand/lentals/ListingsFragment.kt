package com.riceandbeansand.lentals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

import com.google.firebase.firestore.FirebaseFirestore


import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import android.widget.ImageView




class ListingsFragment : Fragment() {

    internal val money_format = DecimalFormat("$0.00")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val db = FirebaseFirestore.getInstance()
        val queryString = getArguments()?.getString("queryType")
        var query = db.collection("items").orderBy("name")

        if (queryString == "mainItems") {
            query = db.collection("items").orderBy("name")
            (activity as AppCompatActivity).supportActionBar!!.title = "Main Listings"
        }
        else if (queryString == "myItems") {
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            query = db.collection("items").orderBy("name").whereEqualTo("userID", userID)
            (activity as AppCompatActivity).supportActionBar!!.title = "My Items"
        }



        val options = FirestoreRecyclerOptions.Builder<ListingsItemSchema>()
                .setQuery(query, ListingsItemSchema::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirestoreRecyclerAdapter<ListingsItemSchema, ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listings_item, parent, false)
                view.setClipToOutline(true)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ListingsItemSchema) {
                if (model.image != null ){
                    val decodedString = Base64.decode(model.image, Base64.DEFAULT)
                    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    holder.view.findViewById<ImageView>(R.id.imageView).setImageBitmap(decodedByte)
                }
                holder.view.findViewById<TextView>(R.id.name).setText(model.name)
                holder.view.findViewById<TextView>(R.id.rate).setText(money_format.format(model.price));
            }
        }

        val view = inflater.inflate(R.layout.listing, container, false)
        val recyclerView = view.findViewById<View>(R.id.item_list) as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter;

        val fab = view.findViewById<View>(R.id.main_fab)
        fab.setOnClickListener(View.OnClickListener {
            activity!!.supportFragmentManager.beginTransaction().addToBackStack(null)
                    .replace(R.id.fragment_container, AddItemFragment()).commit()
        })

        return view
    }

    fun setAdaptorQuery() {

    }

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }

}
