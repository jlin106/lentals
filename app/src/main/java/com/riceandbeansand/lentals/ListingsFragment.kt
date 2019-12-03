package com.riceandbeansand.lentals

import android.content.Context
import android.os.Bundle
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
import android.widget.ImageView

class ListingsFragment : Fragment() {
    private lateinit var dataPasser: OnDataPass

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    interface OnDataPass {
        fun onDataPass(pgUserId: String?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnDataPass) {
            dataPasser = context as OnDataPass
        }
    }

    fun passData(pgUserId: String?) {
        dataPasser.onDataPass(pgUserId)
    }

    internal val money_format = DecimalFormat("$0")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val db = FirebaseFirestore.getInstance()
        val queryString = arguments?.getString("queryType")
        val actualUserId = FirebaseAuth.getInstance().currentUser?.uid.toString();
        val userId = arguments?.getString("userId")
        val searchQuery = arguments?.getString("searchQuery", "")
        var query = db.collection("items").orderBy("name")
        var isCurrentUser = false
        if (userId.equals(actualUserId)) {
            isCurrentUser = true
        }

        if (queryString == "mainItems") {
            passData(null)
            query = db.collection("items").orderBy("name").whereEqualTo("visible", true)
            (activity as AppCompatActivity).supportActionBar!!.title = "Main Listings"
        }
        else if (queryString == "userItems") {
            passData(userId)
            (activity as AppCompatActivity).supportActionBar!!.title = "Profile"
            query = db.collection("items").orderBy("name").whereEqualTo("visible", true).whereEqualTo("userID", userId)
        }
        else if (queryString == "myItems") {
            passData(userId)
            (activity as AppCompatActivity).supportActionBar!!.title = "My Items"
            query = db.collection("items").orderBy("name").whereEqualTo("userID", userId)
        }
        else if (queryString == "searchItems") {
            query = if (userId == null) db.collection("items").orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff")
            else db.collection("items").orderBy("name").whereEqualTo("visible", true).whereEqualTo("userID", userId).startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        }

        val options = FirestoreRecyclerOptions.Builder<ListingsItemSchema>()
                .setQuery(query, ListingsItemSchema::class.java)
                .setLifecycleOwner(this)
                .build()

        val adapter = object : FirestoreRecyclerAdapter<ListingsItemSchema, ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listings_item, parent, false)
                @Suppress
                view.setClipToOutline(true)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ListingsItemSchema) {
                if (model.imagePath != null ) {
                    val imageFile = getImageFileFromGSUrlWithCache(model.imagePath, activity!!.cacheDir){ File ->
                        val decodedBytes = BitmapFactory.decodeFile(File.absolutePath)
                        holder.view.findViewById<ImageView>(R.id.imageView).setImageBitmap(decodedBytes)
                    }
                }
                if (model.userName != null) {
                    val username = "@" + model.userName.toLowerCase().replace(" ", "")
                    holder.view.findViewById<TextView>(R.id.userName).text = username
                }
                if (!model.visible) {
                    holder.view.findViewById<TextView>(R.id.name).setTextColor(getResources().getColor(R.color.red))
                    holder.view.findViewById<TextView>(R.id.rate).setTextColor(getResources().getColor(R.color.red))
                    holder.view.findViewById<TextView>(R.id.userName).setTextColor(getResources().getColor(R.color.red))
                }
                holder.view.findViewById<TextView>(R.id.name).setText(model.name)
                holder.view.findViewById<TextView>(R.id.rate).setText(money_format.format(model.price));

                holder.view.setOnClickListener(View.OnClickListener {
                    if (queryString == "myItems") {
                        // If it is your items, allow user to edit
                        val updateItem = AddItemFragment()
                        val args = Bundle()
                        args.putString("itemID", model.uid)
                        updateItem.arguments = args
                        activity!!.supportFragmentManager.beginTransaction().addToBackStack(null)
                                .replace(R.id.fragment_container, updateItem).commit()
                    } else {
                        val itemProfile = ItemProfileFragment()
                        val args = Bundle()
                        args.putString("itemID", model.uid)
                        itemProfile.arguments = args
                        activity!!.supportFragmentManager.beginTransaction().addToBackStack(null)
                                .replace(R.id.fragment_container, itemProfile).commit()
                    }
                })
            }
        }

        val view = inflater.inflate(R.layout.listing, container, false)
        val recyclerView = view.findViewById<View>(R.id.item_list) as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        val fab = view.findViewById<View>(R.id.main_fab)
        fab.setOnClickListener(View.OnClickListener {
            activity!!.supportFragmentManager.beginTransaction().addToBackStack(null)
                    .replace(R.id.fragment_container, AddItemFragment()).commit()
        })

        if (queryString == "userItems" && !isCurrentUser) {
            fab.visibility=View.INVISIBLE
        }

        return view
    }

    fun setAdaptorQuery() {

    }

    internal inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }

}
