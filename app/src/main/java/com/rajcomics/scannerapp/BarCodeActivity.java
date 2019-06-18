package com.rajcomics.scannerapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class BarCodeActivity extends AppCompatActivity {
    Uri photoURI;
    String currentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;
    Boolean FailedScan = true;
    int Counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code);
        setTitle("Barcode Scanner");
        delete();
        initViews();
        photoURI = dispatchTakePictureIntent();
        Log.d("xyz", photoURI.toString());

    }
    public void delete(){
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }

    }
    public void initViews() {
        Button RescanButton = findViewById(R.id.RescanButton);
        Button NextscanButton = findViewById(R.id.NextscanButton);

        RescanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                purgeData();
                photoURI = dispatchTakePictureIntent();
            }
        });
        NextscanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!FailedScan){
                SaveData();
                photoURI = dispatchTakePictureIntent();
            }}
        });
    }
    private void purgeData(){
        photoURI = null;
        FailedScan = false;
        TextView txtView = (TextView) findViewById(R.id.barcodeText);
        txtView.setText("");

    }
    private void SaveData(){
        Counter = Counter + 1;
        TextView txtView = (TextView) findViewById(R.id.barcodeText);
        CharSequence text = txtView.getText();
        String barcodetext = text.toString();
        SQLiteDatabase mydatabase = openOrCreateDatabase("Barcodes", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Barcodes(Barcode VARCHAR,Name VARCHAR);");
        mydatabase.execSQL("INSERT INTO Barcodes VALUES('' ,'');");
        Toast.makeText(getApplicationContext(),barcodetext +  " saved",Toast.LENGTH_SHORT).show();
    }

    private Uri dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.rajcomics.scannerapp.fileprovider",
                        photoFile);
                Log.d("xyz", photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        return photoURI;
    }
    public void main() throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
        /*Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.puppy3);*/

        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();
        if(!detector.isOperational()){
            Log.d("track22", "Detector not set");
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        int n = barcodes.size();
        Log.d("track22", String.valueOf(n));
        if (n == 0){
            Log.d("track22", "Array size 0");
            TextView txtView = (TextView) findViewById(R.id.barcodeText);
            txtView.setText("Barcode Not Detected");
            FailedScan = true;
            return;

        }
        /*if (barcodes.valueAt(0) == null){
            Log.d("track22", "null value found");
            return;
        }*/
        if (barcodes.valueAt(0)!= null){
        Barcode thisCode = barcodes.valueAt(0);
        TextView txtView = (TextView) findViewById(R.id.barcodeText);
        txtView.setText(thisCode.rawValue);
        FailedScan = false;
        }
    }
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView BarcodeImage = findViewById(R.id.barcodeView);
        //TextView barcode = (TextView) findViewById(R.id.barcodeText);

        if (requestCode == 1) {
            Bitmap bitmap = null;
            Log.d("xyz", photoURI.toString());
            try {
               bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BarcodeImage.setImageBitmap(bitmap);

        }
        try {
            main();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}



