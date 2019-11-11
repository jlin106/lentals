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


class ListingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        (activity as AppCompatActivity).supportActionBar!!.title = "Main Listings"

        val db = FirebaseFirestore.getInstance()
        val l = ListingsItemSchema()
        Log.d("App", "L is $l")

        val query = db.collection("items").orderBy("name")
        val options = FirestoreRecyclerOptions.Builder<ListingsItemSchema>()
                .setQuery(query, ListingsItemSchema::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirestoreRecyclerAdapter<ListingsItemSchema, ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listings_item, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ListingsItemSchema) {
                holder.view.findViewById<TextView>(R.id.name).setText(model.name)
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

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }

}
