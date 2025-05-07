package com.example.imageprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivityHighDynamicRange extends AppCompatActivity {

    Button backButton, photoButton, hdrButton;
    ImageView previewImage;
    TextView hdrInfo, previewLabel, tagline;

    boolean image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_high_dynamic_range);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityHighDynamicRange.this, MainActivity.class);
            startActivity(intent);
        });

        previewImage = findViewById(R.id.previewImage);
        hdrInfo = findViewById(R.id.hdrInfoText);
        previewLabel = findViewById(R.id.previewLabel);
        tagline = findViewById(R.id.tagline);

        image = false;

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityHighDynamicRange.this, ActivityCamera.class);
            intent.putExtra("setRequired", 3);
            startActivity(intent);
        });

        hdrButton = findViewById(R.id.hdrButton);
        hdrButton.setOnClickListener(v -> {
            if (!image) {
                hdrButton.setText(R.string.hdr_info_button);
                hdrInfo.setVisibility(View.INVISIBLE);
                previewImage.setVisibility(View.VISIBLE);
                image = true;
            } else {
                hdrButton.setText(R.string.show_images_button);
                hdrInfo.setVisibility(View.VISIBLE);
                previewImage.setVisibility(View.INVISIBLE);
                image = false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}