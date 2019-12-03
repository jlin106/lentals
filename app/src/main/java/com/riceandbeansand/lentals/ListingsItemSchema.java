package com.riceandbeansand.lentals;

import android.util.Log;

import com.google.firebase.firestore.DocumentId;

//FirebaseRecyclerView needs this extra class.
//Complained about missing no-arg constructor when have this in kotlin file which is disgusting and jank
public class ListingsItemSchema {
    @DocumentId
    public String uid;
    public String name;
    public double price;
    public String image;
    public String userName;
    public String userID;
    public String profileID;
    public String descrip;
    public boolean visible;
    public String profilePicture;

    // Needed for Firebase
    public void constructor() {
        Log.d("test,","test");
    }
}
