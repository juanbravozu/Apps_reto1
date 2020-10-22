package com.juanbravo.reto1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button continueBtn;
    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);

       continueBtn = findViewById(R.id.main_continue_btn);
       name = findViewById(R.id.main_name_edt);

       continueBtn.setOnClickListener(
               (v) -> {
                   Intent intent = new Intent(this, MapsActivity.class);
                   intent.putExtra("id", UUID.randomUUID().toString());
                   intent.putExtra("name", name.getText().toString().trim());
                   startActivity(intent);
               }
       );
    }
}