package com.example.imageprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ImageProcessor extends AppCompatActivity {
    private int setRequired = 10;
    private String processID = "NM";

    private Bitmap processedImage;

    private final List<Future<PixelRowUpdate>> futures = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_processor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(() -> {
            try {
                ImageProcessFactory();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Future<PixelRowUpdate> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                Intent intent = new Intent(ImageProcessor.this, MainActivityNoiseMasking.class);
                intent.putExtra("processedImage", processedImage);
                startActivity(intent);
                // finish();
            });
        }).start();
    }

    private void ImageProcessFactory() throws IOException {
       File[] rawData;
        File[] dngFiles = getCacheDir().listFiles((dir, name) -> name.endsWith(".dng"));
       if (dngFiles != null && dngFiles.length >= 10) {
           Arrays.sort(dngFiles, Comparator.comparingLong(File::lastModified).reversed());
           rawData = Arrays.copyOfRange(dngFiles, 0, setRequired);
       } else {
           Toast.makeText(this, "Not enough DNG files", Toast.LENGTH_SHORT).show();
           return;
       }

        //load in the DNG files as Bitmap
        Bitmap[] bitmaps = new Bitmap[setRequired];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            for (int i = 0; i < setRequired; i++) {
                File file = rawData[i];
                ImageDecoder.Source source = ImageDecoder.createSource(file);
                bitmaps[i] = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                });
            }
        }

        //setup size of output Bitmap
        int width = bitmaps[0].getWidth();
        int height = bitmaps[0].getHeight();
        Bitmap processedOutput = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CORES);

        for (int y = 0; y < height; y++) {
            int finalY = y;
            Future<PixelRowUpdate> future = executorService.submit(() -> {
                int[] pixelUpdates = new int[width];
                for (int x = 0; x < width; x++) {
                    // Calculates average luminance of a given pixel
                    double[] luminances = new double[setRequired];
                    double[] lumVariances = new double[setRequired];
                    for (int i = 0; i < setRequired; i++) {
                        Bitmap bmp = bitmaps[i];
                        int color = bmp.getPixel(x, finalY);
                        int r = Color.red(color);
                        int g = Color.green(color);
                        int b = Color.blue(color);
                        luminances[i] = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                    }
                    double avgLuminance = Arrays.stream(luminances).sum() / setRequired;
                    for (int i = 0; i < setRequired; i++) {
                        lumVariances[i] = Math.pow(luminances[i] - avgLuminance, 2);
                    }
                    double avgLuminanceSquare = Arrays.stream(lumVariances).sum() / setRequired;
                    int gray = Math.min(255, (int) (Math.sqrt(avgLuminanceSquare) * 2));
                    pixelUpdates[x] = Color.rgb(gray, gray, gray);
                }
                return new PixelRowUpdate(finalY, pixelUpdates);
            });
            futures.add(future);
        }

        executorService.submit(() -> {
            // Wait for all tasks to finish and apply updates
            for (Future<PixelRowUpdate> future : futures) {
                try {
                    PixelRowUpdate update = future.get();
                    for (int x = 0; x < width; x++) {
                        processedOutput.setPixel(x, update.y, update.colors[x]);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }
}
