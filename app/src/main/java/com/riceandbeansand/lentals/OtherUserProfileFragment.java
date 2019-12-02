package com.riceandbeansand.lentals;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class OtherUserProfileFragment extends Fragment {

    private String userID;
    private static final String TAG = "DocSnippets";
    private String userName;
    private String usrProf;

    //TODO: Connect this class to mainActivity
    //TODO: make collection for
    //itemlisting fragment already exists, you can just put a container for the item list fragment in the scrollview fragment

    public View OnCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.other_user_profile, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Other User Profile");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            userID = bundle.getString("itemID", "");
        }

        final TextView nameIP = (TextView) view.findViewById(R.id.other_username);
        final TextView handleIP = (TextView) view.findViewById(R.id.other_userhandle);
        final ImageView imageIP = (ImageView) view.findViewById(R.id.other_profile);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference item = db.collection("items").document(userID);
        item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("USER EXISTS");
                        usrProf = document.getString("image");
                        userName = document.getString("userName");
                        //handleIP = document.getString("userName");

                        nameIP.setText(userName);
                        byte[] decodedString = Base64.decode(usrProf, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageIP.setImageBitmap(decodedByte);

                    }
                }
            }
        });


        return view;
    }

}
