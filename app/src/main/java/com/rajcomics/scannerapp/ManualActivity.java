package com.rajcomics.scannerapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManualActivity extends AppCompatActivity {
    int Manualtext;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    public int counter = 0;
    public int[] codes = new int[40];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        initViews();
    }
    public void initViews(){
        list = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
        list.setAdapter(adapter);
        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purge();
            }
        });
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        final TextView numberText = findViewById(R.id.numberText);
        numberText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if(numberText.getText().length() == 4){
                    String xyz = numberText.getText().toString();
                    Manualtext = Integer.parseInt(xyz);
                    numberText.setText("");
                    addItem(xyz);
                    counter = counter + 1;
                    return true;
                    }
                    else{
                        Log.d("Force", "attempt");
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(numberText, InputMethodManager.SHOW_FORCED );

                    }
                }
                return false;
            }
        });
    }
    public void purge(){
        //codes = null;
        //String[] subjects= new String[]{
        //};
        //List subject_list = new ArrayList<String>(Arrays.asList(subjects));
        //ArrayAdapter arrayadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, subject_list);
        if(counter > 0){
        counter = counter - 1;
        adapter.remove(adapter.getItem(counter));}
        //list.setAdapter(arrayadapter);
    }
    public void addItem(String xyz){
        codes[counter] = Manualtext;
        adapter.add(xyz);
    }
    public void upload(){
        String filename = "arrayFile";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            for (int s : codes) {
                outputStream.write(s);
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

