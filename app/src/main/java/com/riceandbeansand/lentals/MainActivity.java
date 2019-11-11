package com.riceandbeansand.lentals;

import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment mainListingsFragment;
    private Fragment myItemsFragment;
    private FirebaseAuth mAuth;
    private boolean loggedIn = false; //this should be set in the Firebase db, here temporarily
    private FragmentTransaction transaction;
    String userName = "JOHN DOE"; //default user name


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        //setup view
        mainListingsFragment = new ListingsFragment();
        myItemsFragment = new MyItemsFragment();
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainListingsFragment).commit();


        //create toolbars
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Main Listings");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name and profile photo Url
            userName = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();

            TextView nameLabel = (TextView) findViewById(R.id.nameLabel);
            nameLabel.setText(userName);

            // Doesn't work
            ImageView proPic = (ImageView) findViewById(R.id.userProfilePic);
            proPic.setImageURI(photoUrl);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.toMainListings) {
            // launch Main Listings fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainListingsFragment).commit();

        } else if (id == R.id.toMyItems) {
            // launch My Items fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, myItemsFragment).commit();
        } else if (id == R.id.logOut) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


}