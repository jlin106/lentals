package com.riceandbeansand.lentals;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AddItem extends AppCompatActivity {

    SharedPreferences sharedPrefs;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additem);
        context = getApplicationContext();

        final Button postItem = findViewById(R.id.postBtn);
        postItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TO DO: add item to database, return to where ever intent was sent
                Intent intent = new Intent(AddItem.this, MainListings.class);
                startActivity(intent);
            }
        });

        final Button cancelItem = findViewById(R.id.noPostBtn);
        cancelItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }
}
