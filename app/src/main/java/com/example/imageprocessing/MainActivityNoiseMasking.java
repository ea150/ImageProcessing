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

public class MainActivityNoiseMasking extends AppCompatActivity {

    Button backButton, photoButton, nmButton;
    ImageView previewImage;
    TextView nmInfo, previewLabel, tagline;

    boolean image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_noise_masking);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityNoiseMasking.this, MainActivity.class);
            startActivity(intent);
        });

        previewImage = findViewById(R.id.previewImage);
        nmInfo = findViewById(R.id.nmInfoText);
        previewLabel = findViewById(R.id.previewLabel);
        tagline = findViewById(R.id.tagline);

        image = false;

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(v -> {
            // TODO This is where we will use the camera class to take picture
            //  we will also need to modify the imageview if we are going to display side by side images.
            previewImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mountain, getTheme()));
            previewImage.setVisibility(View.VISIBLE);
            previewLabel.setVisibility(View.INVISIBLE);
            tagline.setVisibility(View.INVISIBLE);
        });

        nmButton = findViewById(R.id.nmButton);
        nmButton.setOnClickListener(v -> {
            if (!image) {
                nmButton.setText(R.string.nm_info_button);
                nmInfo.setVisibility(View.INVISIBLE);
                previewImage.setVisibility(View.VISIBLE);
                image = true;
            } else {
                nmButton.setText(R.string.show_images_button);
                nmInfo.setVisibility(View.VISIBLE);
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