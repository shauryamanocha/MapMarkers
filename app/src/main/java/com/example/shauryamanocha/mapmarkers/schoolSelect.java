package com.example.shauryamanocha.mapmarkers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class schoolSelect extends AppCompatActivity {

    boolean schoolSelected = false;
    static String school  = "Not Selected";
    Button startCollecting;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final Spinner schoolSelect = (Spinner)findViewById(R.id.schools);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.schoolList,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolSelect.setAdapter(adapter);
        schoolSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                school = (String)parent.getItemAtPosition(position);
                schoolSelected = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                schoolSelected = false;

            }
        });

        startCollecting = (Button)findViewById(R.id.startCollecting);
        startCollecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(schoolSelect.this, MapsActivity.class));
                Log.w(mainPage.TAG,"clicked");
            }
        });
    }
}
