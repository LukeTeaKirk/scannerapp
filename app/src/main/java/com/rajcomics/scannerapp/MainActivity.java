package com.rajcomics.scannerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Raja Pocket Books");
        initViews();
    }
    public void initViews(){
        Button coverButton = findViewById(R.id.coverButton);
        final Button manualButton = findViewById(R.id.manualButton);

        Button barcodeButton = findViewById(R.id.barcodeButton2);

        barcodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ScanBarcode();
            }
        });
        coverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TakePicture();
            }
        });
        manualButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ManualEntry();

            }
        });


    }
    public void ManualEntry(){
        Intent intent = new Intent(this, ManualActivity.class);
        startActivity(intent);
    }
    public void ScanBarcode(){
        Intent intent = new Intent(this, BarCodeActivity.class);
        startActivity(intent);
    }
    public void TakePicture(){
        Intent intent2 = new Intent(this, CoverActivity.class);
        startActivity(intent2);
    }
}
