package com.example.imageprocessing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityCamera extends AppCompatActivity {

    private TextureView textureView;
    private CameraDevice camera;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageReader imageReader;

    private CameraCharacteristics cameraCharacteristics;
    private TotalCaptureResult captureResult;

    private static final int REQUEST_CAMERA = 1;

    //CONTEXT: For simplicity of implementation.
    // Could be updated in future versions for more robust handling
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        Button captureButton = findViewById(R.id.CaptureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeRawPicture();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA);
        }
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            Size previewSize = cameraCharacteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cam) {
                    camera = cam;
                    startPreview(previewSize);
                }

                @Override public void onDisconnected(@NonNull CameraDevice cam) { cam.close(); }
                @Override public void onError(@NonNull CameraDevice cam, int error) { cam.close(); }
            }, null);

        } catch (Exception e) {
            Toast.makeText(this, "Camera open failed", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: for NM, replace preview with light sensor and prompt to capture on 0 light
    private void startPreview(Size size) {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            Surface surface = new Surface(surfaceTexture);

            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Toast.makeText(ActivityCamera.this, "Preview failure", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, null);

        } catch (Exception e) {
            Toast.makeText(this, "Preview error", Toast.LENGTH_SHORT).show();
        }
    }

    private void takeRawPicture() {
        if (camera == null) return;

        try {
            Size[] rawSizes = cameraCharacteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.RAW_SENSOR);
            Size rawSize = rawSizes[0];

            //TODO: buffer will need to be extracted to a variable and passed in (3 or 10 respectively)
            int burstSize = 10; //TODO: for dev only
            imageReader = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(), ImageFormat.RAW_SENSOR, burstSize);
            Surface rawSurface = imageReader.getSurface();

            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                int savedCount = 0;

                @Override
                public void onImageAvailable(ImageReader reader) {
                    try (Image image = reader.acquireNextImage()) {
                        if (image == null) return;

                        File dngFile = new File(getCacheDir(), "raw_burst_" + System.currentTimeMillis() + "_" + savedCount + ".dng");

                        DngCreator dngCreator = new DngCreator(cameraCharacteristics, captureResult);
                        try (FileOutputStream output = new FileOutputStream(dngFile)) {
                            dngCreator.writeImage(output, image);
                            savedCount++;
                            if (savedCount >= burstSize) {
                                Toast.makeText(ActivityCamera.this, "RAW burst complete", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(ActivityCamera.this, "Error saving burst image", Toast.LENGTH_SHORT).show();
                    }
                }
            }, null);

            List<CaptureRequest> burstList = new ArrayList<>();
            for (int i = 0; i < burstSize; i++) {
                CaptureRequest.Builder rawBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                rawBuilder.addTarget(rawSurface);
                burstList.add(rawBuilder.build());
            }

            camera.createCaptureSession(Arrays.asList(rawSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.captureBurst(burstList, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                           @NonNull CaptureRequest request,
                                                           @NonNull TotalCaptureResult result) {
                                captureResult = result;
                            }

                            @Override
                            public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,int sequenceId, long frameNumber) {
                                Toast.makeText(ActivityCamera.this, "All burst captures done", Toast.LENGTH_SHORT).show();
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        Toast.makeText(ActivityCamera.this, "Burst capture failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(ActivityCamera.this, "Session config failed", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.close();
            camera = null;
        }
        super.onPause();
    }
}