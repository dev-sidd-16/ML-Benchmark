package com.example.siddprakash.mlbenchmarkgroup26;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class testingData extends AppCompatActivity {

    private Evaluation eval;
    private Button backButton;
    private Button logButton;
    private double testTimeElapsed;
    private double trainTimeElapsed;
    private TextView tv;
    private TextView tvSplit;
    private String model;
    private String logFileName = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/logFile.txt";
    private String TAG = "MLBENCHMARK";

    private String TRR, FRR, TAR, FAR, HTER, trainTime, testTime, testSummary, split, cv, k_nn, kernel;
    private String timeStamp;
    private String log = "";
    private File logFile;
    private Bundle bundle;
    private boolean vIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_data);

        logButton = (Button) findViewById(R.id.logbutton);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            logButton.setEnabled(false);
        }
        else {
            logFile = new File(logFileName);
        }

        bundle = this.getIntent().getExtras();
        if (bundle != null) {
            timeStamp = (String) bundle.getSerializable("TimeStamp");
            vIntent = (Boolean) bundle.getSerializable("ViewIntent");
            split = (String)bundle.getSerializable("DataSplit");
            model = (String) bundle.getSerializable("Model");
            eval = (Evaluation) bundle.getSerializable("EvalModel");
            testTimeElapsed = (double) bundle.getSerializable("TestTime");
            trainTimeElapsed = (double) bundle.getSerializable("TrainTime");
        }

        String[] modelParams = model.split("_");

        model = modelParams[0]; // Model available
        k_nn = "5";

        log = "=================================================\n";
        log = log+"TimeStamp: "+timeStamp+"\n";

        tv = (TextView) findViewById(R.id.tr_label);
        switch (model){
            case "LR" : tv.setText("Logistic Regression");
                model = "Logistic Regression";
                cv = modelParams[1];
                log = log+"Model: "+model+"\n";
                log = log+"Cross Validation: "+cv+"\n";
                break;
            case "NB" : tv.setText("Naive Bayes");
                model = "Naive Bayes";
                cv = modelParams[1];
                log = log+"Model: "+model+"\n";
                log = log+"Cross Validation: "+cv+"\n";
                break;
            case "KNN" : tv.setText("k-Nearest Neighbor");
                model = "k-Nearest Neighbor";
                cv = modelParams[2];
                k_nn = modelParams[1];
                log = log+"Model: "+model+"\n";
                log = log+"Cross Validation: "+cv+"\n";
                log = log+"# of Nearest Neighbors: "+k_nn+"\n";
                break;
            case "SVM" : tv.setText("Support Vector Machine");
                model = "Support Vector Machine";
                cv = modelParams[2];
                switch (modelParams[1]){
                    case "poly2": kernel = "Polynomial (Degree=2)";
                        break;
                    case "poly3": kernel = "Polynomial (Degree=3)";
                        break;
                    case "rbf": kernel = "Radial Basis Function";
                        break;
                    default: kernel = "Linear";
                }
                log = log+"Model: "+model+"\n";
                log = log+"Cross Validation: "+cv+"\n";
                log = log+"SVM Kernel: "+kernel+"\n";
                break;

        }

        tvSplit = (TextView) findViewById(R.id.test_label);
        tvSplit.setText(split); // Split available

        log = log+"DB Split %ge: "+split+"\n";

        if(!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Log File Created!");
        }
        else{
            Log.d(TAG, "Log File exists!");
        }

        testSummary = eval.toSummaryString("\nPerformance Summary\n======\n", false); // Test summary available

        double trr = eval.trueNegativeRate(0) * 100;
        double tar = eval.truePositiveRate(0) * 100;
        double frr = eval.falseNegativeRate(0) * 100;
        double far = eval.falsePositiveRate(0) * 100;
        double hter = (frr + far) / 2.0;

        TextView textViewTRR = (TextView) findViewById(R.id.trr);
        textViewTRR.setText(Double.toString(trr)+" %");

        TextView textViewTAR = (TextView) findViewById(R.id.tar);
        textViewTAR.setText(Double.toString(tar)+" %");

        TextView textViewFRR = (TextView) findViewById(R.id.frr);
        textViewFRR.setText(Double.toString(frr)+" %");

        TextView textViewFAR = (TextView) findViewById(R.id.far);
        textViewFAR.setText(Double.toString(far)+" %");

        TextView textViewHTER = (TextView) findViewById(R.id.hter);
        textViewHTER.setText(Double.toString(hter)+" %");

        TextView textViewTrainTime = (TextView) findViewById(R.id.train_time);
        textViewTrainTime.setText(Double.toString(trainTimeElapsed)+" ms.");

        TextView textViewTestTime = (TextView) findViewById(R.id.test_time);
        textViewTestTime.setText(Double.toString(testTimeElapsed)+" ms.");

        TRR = Double.toString(trr);
        FRR = Double.toString(frr);
        TAR = Double.toString(tar);
        FAR = Double.toString(far);
        HTER = Double.toString(hter);
        trainTime = Double.toString(trainTimeElapsed);
        testTime = Double.toString(testTimeElapsed);

        log = log+"Training Time: "+trainTime+" ms\n";
        log = log+"Testing Time: "+testTime+" ms\n";
        log = log+"True Accept Rate (TAR): "+TAR+" %\n";
        log = log+"True Reject Rate (TRR): "+TRR+" %\n";
        log = log+"False Accept Rate (FAR): "+FAR+" %\n";
        log = log+"False Reject Rate (FRR): "+FRR+" %\n";
        log = log+"HTER: "+HTER+"\n";
        log = log+testSummary;

        /*
        Format of log File for single experiment:
        ===============================================================================
         TimeStamp: 2017-12-06T15:49:56Z
         Model: Support Vector Machine
         Cross Validation: 5
         SVM Kernel: Linear
         DB Split %ge: 0.5
         Training Time: 500 ms
         Testing Time: 500 ms
         True Accept Rate (TAR): 0.5
         True Reject Rate (TRR): 0.5
         False Accept Rate (FAR): 0.5
         False Reject Rate (FRR): 0.5
         HTER: 0.5
         Performance Summary
         ======
         Correctly Classified Instances         341               97.7077 %
         Incorrectly Classified Instances         8                2.2923 %
         Kappa statistic                          0.9469
         Mean absolute error                      0.0235
         Root mean squared error                  0.1517
         Relative absolute error                  5.167  %
         Root relative squared error             32.4187 %
         Total Number of Instances              349
         ===============================================================================
         */


        System.out.println(log);

        if(!vIntent) {
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(logFile, true);
                outputStream.write(log.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        backButton = (Button) findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testingData.this, MainActivity.class);
                startActivity(intent);
            }
        });


        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testingData.this, ViewLogFile.class);
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
