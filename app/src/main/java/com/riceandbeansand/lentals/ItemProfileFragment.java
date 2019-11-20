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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

public class ItemProfileFragment extends Fragment {

    private String itemID;
    private static final String TAG = "DocSnippets";
    DecimalFormat money_format = new DecimalFormat("$0.00");
    private String name;
    private double price;
    private String image;
    private String userName;
    private String descrip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.item_profile, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Item Profile");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            itemID = bundle.getString("itemID", "");
        }

        final TextView nameIP = (TextView) view.findViewById(R.id.name_ip);
        final TextView rateIP = (TextView) view.findViewById(R.id.rate_ip);
        final TextView descripIP = (TextView) view.findViewById(R.id.descrip_ip);
        final TextView userNameIP = (TextView) view.findViewById(R.id.userName_ip);
        final ImageView imageIP = (ImageView) view.findViewById(R.id.ImageView_ip);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference item = db.collection("items").document(itemID);
        item.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("DOCUMENT EXISTS");
                        name = document.getString("name");
                        price = document.getDouble("price");
                        image = document.getString("image");
                        userName = document.getString("userName");
                        descrip = document.getString("descrip");

                        nameIP.setText(name);
                        rateIP.setText(money_format.format(price));
                        descripIP.setText(descrip);
                        userNameIP.setText(userName);

                        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageIP.setImageBitmap(decodedByte);

                    }
                }
            }
        });

        return view;
    }
}
