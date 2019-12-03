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
import kotlin.Unit;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.text.DecimalFormat;

public class ItemProfileFragment extends Fragment {

    private static final String TAG = "DocSnippets";
    DecimalFormat money_format = new DecimalFormat("$0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.item_profile, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Item Profile");

        Bundle bundle = this.getArguments();
        String itemID = "";
        if (bundle != null) {
            itemID = bundle.getString("itemID", "");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();

        final TextView nameIP = (TextView) view.findViewById(R.id.name_ip);
        final TextView rateIP = (TextView) view.findViewById(R.id.rate_ip);
        final TextView descripIP = (TextView) view.findViewById(R.id.descrip_ip);
        final TextView userNameIP = (TextView) view.findViewById(R.id.userName_ip);
        final ImageView imageIP = (ImageView) view.findViewById(R.id.imageView_ip);
        view.findViewById(R.id.profilePictureContainer).setClipToOutline(true);
        final Button messageBtn = (Button) view.findViewById(R.id.message_btn);
        final ImageView profilePictureIP = (ImageView) view.findViewById(R.id.userProfilePic_ip);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").document(itemID).get().addOnSuccessListener((DocumentSnapshot doc) -> {
            if (currentUserID.equals(doc.getString("userID"))) {
                messageBtn.setVisibility(View.GONE);
            }

            nameIP.setText(doc.getString("name"));
            rateIP.setText(money_format.format(doc.getDouble("price")));
            descripIP.setText(doc.getString("descrip"));
            userNameIP.setText(doc.getString("userName"));
            messageBtn.setText("Message");

            db.collection("users").document(doc.getString("userID")).get().addOnSuccessListener(userDoc -> {
                String profileString = userDoc.getString("picture");
                try{
                    if (profileString != null && !profileString.isEmpty()) {
                        profilePictureIP.setImageBitmap(stringToBitmap(profileString));
                    }
                } catch (Exception e) {
                }
            });

            UtilityKt.getImageFileFromGSUrlWithCache(doc.getString("imagePath"), getActivity().getCacheDir(), (File file) -> {
                Bitmap decodedBytes = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageIP.setImageBitmap(decodedBytes);
                return Unit.INSTANCE; //required by Java for kotlin interop
            });

            profilePictureIP.setOnClickListener(v -> {
                if (currentUserID.equals(doc.getString("userID"))) {
                    Bundle args = new Bundle();
                    args.putString("queryType", "myItems");
                    args.putString("userId", currentUserID);
                    Fragment myProfile = new ListingsFragment();
                    myProfile.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .replace(R.id.fragment_container, myProfile).commit();
                }
                else {
                    Bundle args = new Bundle();
                    args.putString("name", doc.getString("userName"));
                    args.putString("userId", doc.getString("userID"));
                    Fragment userProfile = new UserProfileFragment(); // userProfile fragment
                    userProfile.setArguments(args);
                    Log.d("App", "setting bundle args " + args.toString());
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                            .replace(R.id.fragment_container, userProfile).commit();
                }
            });

            messageBtn.setOnClickListener(v -> {
                Bundle args = new Bundle();
                String userID = doc.getString("userID");
                boolean lesser = currentUserID.compareTo(userID) < 0; //need chatID that is same if currentUserID and userId are swapped. So always put "lesser" id first.
                String chatID = lesser ? currentUserID + userID : userID + currentUserID; //this is how the chatID is defined; not safe since userId might not be defined yet
                args.putString("chatID", chatID);
                args.putString("name", doc.getString("userName"));
                Fragment chatFragment = new ChatFragment();
                chatFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.fragment_container, chatFragment).commit();
            });
        });
        return view;
    }

    private Bitmap stringToBitmap(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
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
