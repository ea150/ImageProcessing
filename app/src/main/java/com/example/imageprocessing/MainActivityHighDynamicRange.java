package com.example.imageprocessing;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivityHighDynamicRange extends AppCompatActivity {

    Button backButton, photoButton, hdrButton;
    ImageView previewRawImage, previewProcessedImage;
    TextView hdrInfo, previewLabel, tagline;

    boolean processedImageExists;

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

        previewRawImage = findViewById(R.id.previewRawImage);
        previewProcessedImage = findViewById(R.id.previewProcessedImage);
        hdrInfo = findViewById(R.id.hdrInfoText);
        previewLabel = findViewById(R.id.previewLabel);
        tagline = findViewById(R.id.tagline);
        hdrButton = findViewById(R.id.hdrButton);
        photoButton = findViewById(R.id.photoButton);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float deviceRatio = (float) metrics.heightPixels / metrics.widthPixels;

        ConstraintLayout.LayoutParams processedParams = (ConstraintLayout.LayoutParams) previewProcessedImage.getLayoutParams();
        processedParams.dimensionRatio = "W," + (1/deviceRatio) + ":1";
        previewProcessedImage.setLayoutParams(processedParams);

        ConstraintLayout.LayoutParams rawParams = (ConstraintLayout.LayoutParams) previewRawImage.getLayoutParams();
        rawParams.dimensionRatio = "W," + (1 / deviceRatio) + ":1";
        previewRawImage.setLayoutParams(rawParams);

        String processedFilepath = getIntent().getStringExtra("processedFilepath");
        String rawFilepath = getIntent().getStringExtra("rawFilepath");
        processedImageExists = processedFilepath != null;

        if (processedImageExists) {
            hdrButton.setText(R.string.hdr_info_button);
            hdrInfo.setVisibility(View.INVISIBLE);
            previewProcessedImage.setImageBitmap(BitmapFactory.decodeFile(processedFilepath));
            previewRawImage.setImageBitmap(BitmapFactory.decodeFile(rawFilepath));
            previewProcessedImage.setVisibility(View.VISIBLE);
            previewRawImage.setVisibility(View.VISIBLE);
        } else {
            hdrButton.setText(R.string.show_images_button);
            hdrInfo.setVisibility(View.VISIBLE);
            previewProcessedImage.setVisibility(View.INVISIBLE);
            previewRawImage.setVisibility(View.INVISIBLE);
        }

        photoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityHighDynamicRange.this, ActivityCamera.class);
            intent.putExtra("setRequired", 3);
            intent.putExtra("processID", "HDR");
            startActivity(intent);
        });

        hdrButton.setOnClickListener(v -> {
            if (!processedImageExists) {
                hdrButton.setText(R.string.hdr_info_button);
                hdrInfo.setVisibility(View.INVISIBLE);
                previewProcessedImage.setVisibility(View.VISIBLE);
                previewRawImage.setVisibility(View.VISIBLE);
                processedImageExists = true;
            } else {
                hdrButton.setText(R.string.show_images_button);
                hdrInfo.setVisibility(View.VISIBLE);
                previewProcessedImage.setVisibility(View.INVISIBLE);
                previewRawImage.setVisibility(View.INVISIBLE);
                processedImageExists = false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}