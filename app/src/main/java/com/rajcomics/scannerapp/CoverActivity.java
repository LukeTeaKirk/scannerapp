package com.rajcomics.scannerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.ImageView;

/*import com.google.android.gms.common.Feature;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.CropHint;
import com.google.cloud.vision.v1.CropHintsAnnotation;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.protobuf.ByteString;
*/
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoverActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    int[] rgb = new int [16];
    String resultText;
    String[][] label2;
    int x = 0;
    int y = 0;
    String text7;
    String entityId;
    Uri photoURI;
    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);
        setTitle("Cover Scanner");
        photoURI = dispatchTakePictureIntent();
    }
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
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
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private File createImageFile() throws IOException {
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
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("tag", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView BarcodeImage = findViewById(R.id.coverView);
        Bitmap bitmap = null;
        if (requestCode == 1) {
            Log.d("xyz", photoURI.toString());
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BarcodeImage.setImageBitmap(bitmap);
        }
        main(bitmap);
    }
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public void getAverageColor(Bitmap bitmap){
        bitmap = toGrayscale(bitmap);
        AverageColor(bitmap);
        /*int white;
        int pixelCount;
        int[] blockaverage = new int[16];
        int blockheight = bitmap.getHeight()/4;
        int blockwidth = bitmap.getWidth()/4;
        for (int count = 1; count <= 16; count += 1) {
            white = 0;
            pixelCount = 1;
            int count2 = 1;
            for (int y = blockheight*(count-1); y < blockheight*count; y++)
            {
                count2 = 1;
                for (int x = blockwidth*(count2 -1); x < blockwidth*(count2); x++)
                {
                    count2++;
                    int c = bitmap.getPixel(x, y);
                    white = white + c;
                    pixelCount++;
                }
            }
            blockaverage[1] = white;///pixelCount;
            String temp = String.valueOf(blockaverage[1]);
            Log.d("color123", temp);
        }*/
    }
    protected void AverageColor(Bitmap bitmap) {
        System.gc();
        int white;
        int redBucket;
        int greenBucket;
        int blueBucket;
        int pixelCount;
        int blocks = 4;
        int blockCount = 1;
        int blockCount2 = 1;
        int count2 = 0;
        int count3 = 1;
        for (int count = 1; count <= 4*4; count += 1) {
            if (count2 < 5){
                count2++;
            }
            if (count2 == 5)
            {
                count2 = 1;
                count3 = count3 + 1;
            }
            if (count3 == 5){
                count3 = 1;
            }
            white = 0;
            pixelCount = 1;
            for (int y = (bitmap.getHeight() / 4) * (count3 - 1); y < (bitmap.getHeight() / 4) * count3; y++) {
                for(int x = (bitmap.getWidth()/4)*(count2-1); x < (bitmap.getWidth()/4)*count2; x++){
                    int c = bitmap.getPixel(x,y);
                    white = white + c;
                    pixelCount++;
                    //Log.d("color", String.valueOf(c));

                }
            }
            rgb[count-1] = white / pixelCount;
            Log.d("color123456", String.valueOf(rgb[count-1]));

        }
    }
    public void gettext(Bitmap bitmap){
        Log.d("label", "text");
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                String resultText = firebaseVisionText.getText();
                                Log.d("label", "text:" + resultText);
                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Float blockConfidence = block.getConfidence();
                                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                                    Point[] blockCornerPoints = block.getCornerPoints();
                                    Rect blockFrame = block.getBoundingBox();
                                    for (FirebaseVisionText.Line line: block.getLines()) {
                                        String lineText = line.getText();
                                        Float lineConfidence = line.getConfidence();
                                        List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        Rect lineFrame = line.getBoundingBox();
                                        for (FirebaseVisionText.Element element: line.getElements()) {
                                            String elementText = element.getText();
                                            Float elementConfidence = element.getConfidence();
                                            List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            Rect elementFrame = element.getBoundingBox();
                                        }
                                    }
                                }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });


    }
    public void database(Bitmap bitmap){
        for (x = 1; x < 3; x++){
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String s = "sdcard/android/data/com.rajcomics.scannerapp/files/pictures/" + x + ".jpg";
            bitmap = BitmapFactory.decodeFile(s);
            getlabel(bitmap);
            getAverageColor(bitmap);
            gettext(bitmap);
        }
    }
    public void getlabel(Bitmap bitmap){
        Log.d("ML", "1");
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        Log.d("ML", "2");
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();
        Log.d("ML", "3");
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        for (FirebaseVisionImageLabel label: labels) {
                            text7 = label.getText();
                            entityId = label.getEntityId();
                            float confidence = label.getConfidence();
                            Log.d("labels", text7);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });

    }
    public void databasemethod(Bitmap bitmap){
        getlabel(bitmap);
        getAverageColor(bitmap);
        gettext(bitmap);
    }
    public void main(Bitmap bitmap){
        //getlabel(bitmap);
        //getAverageColor(bitmap);
        //gettext(bitmap);
        database(bitmap);
    }
}

