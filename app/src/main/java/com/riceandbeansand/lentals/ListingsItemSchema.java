package com.riceandbeansand.lentals;

import android.util.Log;

//FirebaseRecyclerView needs this extra class.
//Complained about missing no-arg constructor when have this in kotlin file which is disgusting and jank
public class ListingsItemSchema {
    public String name;
    public double price;
    public String image;
    public String userName;
    public String userID;
    public String descrip;

    // Needed for Firebase
    public void constructor() {
        Log.d("test,","test");
    }
}
