package com.riceandbeansand.lentals;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.facebook.login.widget.ProfilePictureView;
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
    private String userId;
    private String profileId;

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
        final ImageView imageIP = (ImageView) view.findViewById(R.id.imageView_ip);
        view.findViewById(R.id.profilePictureContainer).setClipToOutline(true);
        final ProfilePictureView profilePictureIP = (ProfilePictureView) view.findViewById(R.id.userProfilePic_ip);

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
                        userId = document.getString("userID");
                        profileId = document.getString("profileID");

                        nameIP.setText(name);
                        rateIP.setText(money_format.format(price));
                        descripIP.setText(descrip);
                        userNameIP.setText(userName);
                        profilePictureIP.setProfileId(profileId);

                        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageIP.setImageBitmap(decodedByte);

                    }
                }
            }
        });

        profilePictureIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("name", userName);
                args.putString("userId", userId);
                args.putString("profileId", profileId);
                Fragment userProfile = new UserProfileFragment(); // userProfile fragment
                userProfile.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, userProfile).commit();
            }
        });

        final Button messageBtn = (Button) view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFBInstalled = isAppInstalled("com.facebook.orca");

                if (!isFBInstalled) {
                    Toast.makeText(getActivity(),
                            "Facebook messenger isn't installed. Please download the app first.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent= new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Hello, is this still available?");
                    intent.setType("text/plain");
                    intent.setPackage("com.facebook.orca");

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(),
                                "Sorry! Can't open Facebook messenger right now. Please try again later.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
