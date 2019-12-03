package com.riceandbeansand.lentals;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    CallbackManager callbackManager;
    private static final String EMAIL = "email";
    AccessToken accessToken;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hacky fix for network async
//        int SDK_INT = android.os.Build.VERSION.SDK_INT;
//        if (SDK_INT > 8)
//        {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                    .permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
        }

        setContentView(R.layout.login_screen);

        // setup facebook
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPermissions(Arrays.asList(EMAIL));

        getLoginDetails(loginButton);
    }

    protected void getLoginDetails(LoginButton loginButton){
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                findViewById(R.id.login_button).setVisibility(View.GONE);
                TextView label = (TextView) findViewById(R.id.signInLabel);
                label.setText("Logging in....");
                accessToken = loginResult.getAccessToken();
                AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
                mAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("App", "signInWithCredential:success");
                            GraphRequest request = GraphRequest.newMeRequest(
                                    accessToken,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            Log.v("LoginActivity", response.toString());

                                            try {
                                                final String userID = mAuth.getCurrentUser().getUid();
                                                final String name = object.getString("name");
                                                final String email = object.getString("email");
                                                final String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                                try {
                                                    // Async call
                                                    Handler handler = new Handler();
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new AsyncCaller().execute(userID, name, email, profilePicUrl);
                                                        }
                                                    });
                                                }
                                                catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                            catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,picture.type(large)");
                            request.setParameters(parameters);
                            request.executeAsync();

                            finish();

                        } else {
                            Log.w("App", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
            }
            @Override
            public void onCancel() {
                // code for cancellation
            }
            @Override
            public void onError(FacebookException exception) {
                //  code to handle error
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

//     Async inner class
    private class AsyncCaller extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {
                URL url = new URL(params[3]);
                Bitmap profilePic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                profilePic.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                String profPicEncodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                params[3] = profPicEncodedString;
                return params;
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        //TODO: need to wait for this to finish before moving on to main Activity
        //otherwise first time user won't have an ID pic
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            Map<String, Object> docData = new HashMap<>();
            docData.put("name", result[1]);
            docData.put("email", result[2]);
            docData.put("picture", result[3]);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(result[0]).set(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("App", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("App", "Error writing document", e);
                        }
                    });
        }

    }

}