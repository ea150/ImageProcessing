package com.example.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class ImageProcessor extends AppCompatActivity {
    //TODO: extract setRequired and processID from values from the xml within onCreate
    //TODO: (cont) for dev purposes only
    private int setRequired = 10;
    private String processID = "NM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        //TODO: Create button that calls this and outputs to a jpg
//        File[] dngFiles = getCacheDir().listFiles((dir, name) -> name.endsWith(".dng"));
//        Arrays.sort(dngFiles, Comparator.comparingLong(File::lastModified).reversed());
//
//        if (dngFiles.length >= setRequired) {
//            Bitmap outputResult = ImageProcessFactory(Arrays.copyOfRange(dngFiles, 0, setRequired), setRequired, processID);
//            //TODO: map to an actual image view
//            imageView.setImageBitmap(outputResult);
//        } else {
//            Toast.makeText(this, "Not enough DNG files", Toast.LENGTH_SHORT).show();
//        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_processor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Bitmap ImageProcessFactory(File[] rawData, int setRequired, String processID){

        //load in the DNG files as Bitmap
        Bitmap[] bitmaps = new Bitmap[setRequired];
        for (int i = 0; i < setRequired; i++) {
            //TODO: add API requirements....how to handle?
            ImageDecoder.Source source = ImageDecoder.createSource(rawData[i]);
            bitmaps[i] = ImageDecoder.decodeBitmap(source);
        }

        //setup size of output Bitmap
        int width = bitmaps[0].getWidth();
        int height = bitmaps[0].getHeight();
        Bitmap processedOutput = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if(processID.equals("HDR")){
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                long r = 0, g = 0, b = 0;
                for (Bitmap bmp : bitmaps) {
                    int color = bmp.getPixel(x, y);
                    r += Color.red(color);
                    g += Color.green(color);
                    b += Color.blue(color);
                }
                int avgR = (int)(r / 10);
                int avgG = (int)(g / 10);
                int avgB = (int)(b / 10);
                processedOutput.setPixel(x, y, Color.rgb(avgR, avgG, avgB));
            }
        }
        //indicate where in process
        Toast.makeText(this, "Averaging completed. ", Toast.LENGTH_SHORT).show();
        }

        else if(processID.equals("NM")) {

            //NOISE MAP
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double[] luminances = new double[setRequired];

                    for (int i = 0; i < 10; i++) {
                        int color = bitmaps[i].getPixel(x, y);
                        int r = Color.red(color);
                        int g = Color.green(color);
                        int b = Color.blue(color);
                        luminances[i] = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Luminance
                    }

                    double mean = 0;
                    for (double val : luminances) mean += val;
                    mean /= luminances.length;

                    double variance = 0;
                    for (double val : luminances) variance += Math.pow(val - mean, 2);
                    variance /= luminances.length;

                    // scale for visibility
                    double stdDev = Math.sqrt(variance);
                    int gray = Math.min(255, (int) (stdDev * 2));

                    processedOutput.setPixel(x, y, Color.rgb(gray, gray, gray));
                }
            }
        }
        //return the Bitmap
        return processedOutput;
    }



// TODO: delete if not needed in the end
// Thought we would need it, might not

//    private void saveBitmapToJpg(Bitmap bitmap, File outputFile) {
//        try (FileOutputStream out = new FileOutputStream(outputFile)) {
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // 90 = quality (0–100)
//            out.flush();
//        } catch (Exception e) {
//            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
//        }
//    }



}