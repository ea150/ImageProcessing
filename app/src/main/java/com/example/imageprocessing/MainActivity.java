package com.example.imageprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button noiseMaskingButton;
    Button hdrButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        noiseMaskingButton = findViewById(R.id.NoiseMasking);
        noiseMaskingButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this,MainActivityNoiseMasking.class);
            startActivity(i);
        }
        );

        hdrButton = findViewById(R.id.HDR);
        hdrButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this,MainActivityHighDynamicRange.class);
            startActivity(i);
        }
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}