package com.riceandbeansand.lentals;

import android.util.Log;

import com.google.firebase.firestore.DocumentId;

//FirebaseRecyclerView needs this extra class.
//Complained about missing no-arg constructor when have this in kotlin file which is disgusting and jank
public class ChatItemSchema {
    @DocumentId
    public String uid;
    public String message;
    public long timestamp;
    public String senderID;

    // Needed for Firebase
    public void constructor() {
        Log.d("test,","test");
    }
}
