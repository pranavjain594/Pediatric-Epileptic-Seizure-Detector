package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

public class epilepsy extends AppCompatActivity {

    FirebaseCustomRemoteModel remoteModel;
    Interpreter interpreter;
    ByteBuffer input;
    ByteBuffer modelOutput;
    ArrayList<Integer> arr;
    final int READ_REQUEST_CODE = 123;
    Uri uri;
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series,series2;
    private int lastX = 0;
    int i;
    GraphView graph;

    public void detect(View v){
        lastX=0;
        arr.clear();
        graph.removeAllSeries();
        if(uri==null) Toast.makeText(this, "No File Uploaded", Toast.LENGTH_SHORT).show();
        else loadModel();
    }

    public void loadModel(){
        remoteModel = new FirebaseCustomRemoteModel.Builder("epilepsy").build();
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<File>() {
                    @Override
                    public void onComplete(@NonNull Task<File> task) {
                        File modelFile = task.getResult();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                            //Toast.makeText(epilepsy.this, "Hosted Model loaded.. Running interpreter..", Toast.LENGTH_SHORT).show();
                            runningInterpreter();
                        }
                        else{
                            try {
                                InputStream inputStream = getAssets().open("epilepsy.tflite");
                                byte[] model = new byte[inputStream.available()];
                                inputStream.read(model);
                                ByteBuffer buffer = ByteBuffer.allocateDirect(model.length)
                                        .order(ByteOrder.nativeOrder());
                                buffer.put(model);
                                interpreter = new Interpreter(buffer);
                                //Toast.makeText(epilepsy.this, "Bundled Model loaded.. Running interpreter..", Toast.LENGTH_SHORT).show();
                                runningInterpreter();
                            } catch (IOException e) {
                                // File not found?
                                Toast.makeText(epilepsy.this, "No hosted or bundled model", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void runningInterpreter() {

        String line = "";
        try {
            int c=0;
            //Toast.makeText(this, ""+c, Toast.LENGTH_SHORT).show();
            //InputStream inputStream = getResources().openRawResource(R.raw.test2);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(inputStream))) ;
            while ((line = reader.readLine()) != null) {
                // Split the line into different tokens (using the comma as a separator).
                //Toast.makeText(this, line, Toast.LENGTH_SHORT).show();
                String[] tokens = line.split(",");
                input = ByteBuffer.allocateDirect(23 * 4).order(ByteOrder.nativeOrder());
                for (int x = 0; x < 23; x++) {
                    input.putFloat(Float.valueOf(tokens[x]));
                }

                int bufferSize = 1 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
                modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
                if(interpreter==null){
                    Toast.makeText(this, "Interpreter is null", Toast.LENGTH_SHORT).show();
                }
                else{
                    interpreter.run(input, modelOutput);
                }
                predictOutput();
            }

            initialzeGraph(arr.size());

        } catch (IOException e1) {
            Log.e("MainActivity", "Error" + line, e1);
            e1.printStackTrace();
        }
    }

    public void predictOutput(){
        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        try {
            for (int i = 0; i < probabilities.capacity(); i++) {
                float probability = probabilities.get(i);
                arr.add(Math.round(probability));
               // Toast.makeText(this, ""+Math.round(probability), Toast.LENGTH_SHORT).show();
                //return Math.round(probability);
                //Toast.makeText(this, "Output"+c+"="+String.format("%1.4f", probability), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // File not found?
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        }

        public void initialzeGraph(int size){
            // data
            series = new LineGraphSeries<>();
            series2 = new LineGraphSeries<>();

            series.setColor(Color.GREEN);
            series2.setColor(Color.RED);

            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(epilepsy.this, "Data Point : "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            graph.setTitle("Prediction : "+ getFileName(uri));
            // customize a little bit viewport

            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(size);
            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);

            graph.addSeries(series);
            graph.addSeries(series2);


            Toast.makeText(this, "Zoom In And Out For Better Understanding", Toast.LENGTH_SHORT).show();
            showOutput();
        }

    protected void showOutput() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                for (int i = 0; i < arr.size(); i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }

    private void addEntry() {
        try{
            if(arr.get(lastX)==1 && lastX!=arr.size()) series2.appendData(new DataPoint(lastX, arr.get(lastX)), false, arr.size());
            series.appendData(new DataPoint(lastX, arr.get(lastX++)), false, arr.size());
        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epilepsy);
        setTitle("Epilepsy Detection");
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        arr=new ArrayList<>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height/2));
        graph.getGridLabelRenderer().setGridColor(Color.GRAY);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(-1);
        viewport.setMaxY(2);

        remoteModel =
                new FirebaseCustomRemoteModel.Builder("epilepsy").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.about:
                Intent i=new Intent(this,MainActivity4.class);
                startActivity(i);
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case READ_REQUEST_CODE: {
                if (resultCode == RESULT_OK){
                    uri = data.getData();
                    Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, ""+uri, Toast.LENGTH_SHORT).show();
                }
            }
        }
        }

    public void epilepsy(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}