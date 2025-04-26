package com.example.imageprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivityHighDynamicRange extends AppCompatActivity {

    Button backButton, photoButton, hdrButton;
    ImageView previewImage;
    TextView previewLabel, tagline;

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
        previewLabel = findViewById(R.id.previewLabel);
        tagline = findViewById(R.id.tagline);

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(v -> {
            // TODO This is where we will use the camera class to take picture
            previewImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mountain, getTheme()));
            previewImage.setVisibility(View.VISIBLE);
            previewLabel.setVisibility(View.INVISIBLE);
            tagline.setText(R.string.hdr_apply_or_take);
        });

        hdrButton = findViewById(R.id.hdrButton);
        hdrButton.setOnClickListener(v -> {
            // TODO This is where we will apply HDR
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}