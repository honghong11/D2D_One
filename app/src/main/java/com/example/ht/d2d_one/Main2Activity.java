package com.example.ht.d2d_one;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processExtraData();
        Log.d("device_______________","1111111111111111111111");
    }

    private void processExtraData(){
        Intent intent = getIntent();
        String string = intent.getStringExtra("device");
        TextView NAME = findViewById(R.id.name);
        Log.d("device_______________",string);
    }
}
