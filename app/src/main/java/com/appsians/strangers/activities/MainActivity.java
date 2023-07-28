package com.appsians.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.appsians.strangers.R;
import com.appsians.strangers.databinding.ActivityMainBinding;
import com.appsians.strangers.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins = 0;
    String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    User user;
    KProgressHUD progress;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {

            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.show();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        database.getReference().child("profiles")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        progress.dismiss();
                        user = snapshot.getValue(User.class);
                        coins = user.getCoins();

                        binding.coins.setText("You have: " + coins);

                        Glide.with(MainActivity.this)
                                .load(user.getProfile())
                                .into(binding.profilePicture);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionsGranted()) {
                    if (coins > 5) {
                        coins = coins - 5;
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                        intent.putExtra("profile", user.getProfile());
                        startActivity(intent);
                        //startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermissions();
                }
            }
        });

        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });




    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for(String permission : permissions ){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }



    private void initializeCamera() {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            int cameraId = -1;

            // Find the front-facing camera
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                    break;
                }
            }

            if (cameraId == -1) {
                // No front-facing camera found
                Toast.makeText(this, "Front camera not found", Toast.LENGTH_SHORT).show();
                return;
            }

            camera = Camera.open(cameraId);
            camera.setDisplayOrientation(90); // Adjust the orientation as needed

            // Set the camera preview to the SurfaceView
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize camera", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface created, initialize the camera
        initializeCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface changed, update the camera preview if needed
        if (surfaceHolder.getSurface() == null) {
            // Surface is not ready, return
            return;
        }

        // Stop the camera preview
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the camera preview with the updated settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface destroyed, release the camera resources
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


}
