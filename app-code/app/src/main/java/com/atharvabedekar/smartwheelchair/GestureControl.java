package com.atharvabedekar.smartwheelchair;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProcessor;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.camera.core.Preview;

import com.atharvabedekar.smartwheelchair.ml.ModelUnquant;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestureControl extends AppCompatActivity {

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private static final int CAMERA_REQUEST = 101;
    private ExecutorService cameraExecutor;

    private TextView cmdTxt;

    private ImageAnalysis imageAnalyzer;
    ModelUnquant model;
    TensorBuffer inputFeature0;

    String[] classes = {"Forward", "Backward", "Left"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_control);

        previewView = findViewById(R.id.view_finder);
        cmdTxt = findViewById(R.id.txt_cmd);
        Button btnExit = findViewById(R.id.btnExit);
        Button btnStop = findViewById(R.id.btnStop);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpMLModels();
        requestCameraPermission();
    }

    private void setUpMLModels() {
        try {
            model = ModelUnquant.newInstance(getApplicationContext());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "CameraX permission granted", Toast.LENGTH_SHORT).show();
                setUpCamera();
            } else {
                Toast.makeText(this, "CameraX permission denied. Try again", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }
        }
    }

    private void requestCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            setUpCamera();
        }
    }

    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraExecutor = Executors.newSingleThreadExecutor();
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));
    }

    private void classifyImage(ImageProxy image) {
        Bitmap bitmap = toBitmap(image);
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        TensorImage ti = new TensorImage(DataType.FLOAT32);
        ti.load(bitmap);
        inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        inputFeature0.loadBuffer(ti.getBuffer());
        ModelUnquant.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//        Log.e("APPX", outputFeature0.getFloatArray()[0]+" "+outputFeature0.getFloatArray()[1]+" "+outputFeature0.getFloatArray()[2]);
        changeText(classes[getIndexOfBiggest(outputFeature0.getFloatArray())]);
    }

    private Bitmap toBitmap(androidx.camera.core.ImageProxy imageProxy) {
        ByteBuffer yBuffer = imageProxy.getPlanes()[0].getBuffer(), vuBuffer = imageProxy.getPlanes()[2].getBuffer(); // VU

        int ySize = yBuffer.remaining(), vuSize = vuBuffer.remaining();

        byte[] nv21 = new byte[ySize + vuSize];;

        yBuffer.get(nv21, 0, ySize);
        vuBuffer.get(nv21, ySize, vuSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void changeText(String txt) {
        cmdTxt.setText("Command: "+txt);
    }

    private int getIndexOfBiggest(float[] confidences) {
        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }
        return maxPos;
    }

    // Declare and bind preview, capture and analysis use cases
    private void bindCameraUseCases() {

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());


        imageAnalyzer = new ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, image -> {
            classifyImage(image);
            image.close();
        });

        ViewPort viewPort = previewView.getViewPort();

        assert viewPort != null;
        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageAnalyzer)
                    .setViewPort(viewPort)
                    .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    useCaseGroup
            );
        } catch (Exception exc) {
            Log.e("APPX", "Use case binding failed", exc);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.close();
    }
}