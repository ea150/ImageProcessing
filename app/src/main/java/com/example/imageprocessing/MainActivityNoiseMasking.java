package com.example.imageprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivityNoiseMasking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {Button b1;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_noise_masking);

        b1 = findViewById(R.id.BackButton);
        b1.setOnClickListener(
                new View.OnClickListener(){ @Override
                public void onClick(View v){
                    Intent i = new Intent(MainActivityNoiseMasking.this,MainActivity.class);
                    startActivity(i);}
                }
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}