package com.example.siddprakash.mlbenchmarkgroup26;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;


public class ViewLogFile extends AppCompatActivity {

    private Bundle bundle;
    private String logFileName = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/logFile.txt";
    private File logFile;
    private Boolean vIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_log_file);

        bundle = this.getIntent().getExtras();
        vIntent = (Boolean) bundle.getSerializable("ViewIntent");
        vIntent = true;

        TextView tv = (TextView) findViewById(R.id.logView);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            tv.setText("No read oermission! Please go back!");
        }
        else {
            logFile = new File(logFileName);
        }

        StringBuilder logData = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = br.readLine()) != null) {
                logData.append(line);
                logData.append('\n');
            }
            br.close() ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logData != null) {
            tv.setText(logData.toString());
        }
        else{
            tv.setText("Empty file! No log data available!");
        }

        FloatingActionButton backButton = (FloatingActionButton) findViewById(R.id.logBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewLogFile.this, testingData.class);
                bundle.putSerializable("ViewIntent", vIntent);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
