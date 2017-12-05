package com.example.siddprakash.mlbenchmarkgroup26;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class testingData extends AppCompatActivity {

    private Evaluation eval;
    private Button backButton;
    private double testTimeElapsed;
    private double trainTimeElapsed;
    private TextView tv;
    private String model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_data);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            model = (String) bundle.getSerializable("Model");
            eval = (Evaluation) bundle.getSerializable("EvalModel");
            testTimeElapsed = (double) bundle.getSerializable("TestTime");
            trainTimeElapsed = (double) bundle.getSerializable("TrainTime");
        }

        model = model.split("_")[0];

        tv = (TextView) findViewById(R.id.tr_label);
        switch (model){
            case "LR" : tv.setText("Logistic Regression");
                break;
            case "NB" : tv.setText("Naive Bayes");
                break;
            case "KNN" : tv.setText("k-Nearest Neighbor");
                break;
            case "SVM" : tv.setText("Support Vector Machine");
                break;

        }

        System.out.println(eval.toSummaryString("\nResults\n======\n", false));

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

        System.out.println("True Accept Rate (TAR): "+tar+" %");
        System.out.println("True Reject Rate (TAR): "+trr+" %");
        System.out.println("False Accept Rate (TAR): "+far+" %");
        System.out.println("False Reject Rate (TAR): "+frr+" %");
        System.out.println("HTER: "+hter+" %");

        backButton = (Button) findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(testingData.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
