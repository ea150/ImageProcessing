package com.example.imageprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

        String processedFilepath = getIntent().getStringExtra("processedFilepath");
        String rawFilepath = getIntent().getStringExtra("rawFilepath");
        processedImageExists = processedFilepath != null;

        if (processedImageExists) {
            assert rawFilepath != null;

            hdrButton.setText(R.string.hdr_info_button);
            hdrInfo.setVisibility(View.INVISIBLE);

            Bitmap processedBmp = BitmapFactory.decodeFile(processedFilepath);
            Bitmap rawBmp = BitmapFactory.decodeFile(rawFilepath);
            Bitmap processedRotated, rawRotated;
            try {
                Matrix matrix = new Matrix();

                matrix.postRotate(270);

                processedRotated = Bitmap.createBitmap(processedBmp, 0 ,0, processedBmp.getWidth(), processedBmp.getHeight(), matrix, true);
                rawRotated = Bitmap.createBitmap(rawBmp, 0 ,0, rawBmp.getWidth(), rawBmp.getHeight(), matrix, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            previewProcessedImage.setImageBitmap(processedRotated);
            previewRawImage.setImageBitmap(rawRotated);

            int width = processedBmp.getWidth();
            int height = processedBmp.getHeight();
            int gcd = gcd(width, height);

            width /= gcd;
            height /= gcd;

            ConstraintLayout.LayoutParams processedParams = (ConstraintLayout.LayoutParams) previewProcessedImage.getLayoutParams();
            ConstraintLayout.LayoutParams rawParams = (ConstraintLayout.LayoutParams) previewRawImage.getLayoutParams();

            processedParams.dimensionRatio = "W," + width + ":" + height;
            rawParams.dimensionRatio = "W," + width + ":" + height;

            previewProcessedImage.setLayoutParams(processedParams);
            previewRawImage.setLayoutParams(rawParams);

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

    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}