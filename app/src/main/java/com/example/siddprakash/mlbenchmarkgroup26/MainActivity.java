package com.example.siddprakash.mlbenchmarkgroup26;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.pmml.jaxbbindings.False;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MLBENCHMARK";

    private float dataSplit = 0.5f;
    private int algorithm = 0;
    private int svmK = 0;
    private String model = "lr.model";
    private boolean downloaded = false;
    private double elapsedTrainSeconds = 0.0;
    private double elapsedSeconds = 0.0;

    private EditText dSplit;
    private EditText cv;
    private String crossValidation = "5";
    private String k_nn = "5";
    private Spinner algo;
    private Spinner svmKernel;
    private Button saveParams;
    private Button trainButton;
    private Button testButton;
    private TextView knnParam;
    private TextView svmParam3;
    private EditText knn;
    boolean begin = true;
    private Context context;
    private String trainData;
    private String mname = "LR_";
    private String finalMName = mname;

    private String appFolderPath = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/";
    private String trainFile = "trainFile.arff";
    private String testFile = "testFile.arff";
    private static final String uploadFilePath = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/";
    private static final String downloadFilePath = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/";
    public static final String upLoadURL = "http://ec2-54-219-146-0.us-west-1.compute.amazonaws.com:8080/upload/";
    public static final String downloadUrl = "http://ec2-54-219-146-0.us-west-1.compute.amazonaws.com:8080/train/";
    //public static final String downloadUrl = "http://10.152.114.187:8080/train/SVM";



    private static final int REQUEST_ID_READ_PERMISSION = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION = 200;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int ACCESS_EXTERNAL_STORAGE_STATE = 1;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ACCESS_EXTERNAL_STORAGE_STATE);
        }

        mname = "LR_";

        TextView tv = (TextView) findViewById(R.id.modelName);
        File modelFile = new File(downloadFilePath+model);
        System.out.println("file......."+downloadFilePath+model);

        if (!modelFile.exists()) {
            tv.setText("No");
            System.out.println("Does not exists..............");
        }
        else{
            modelFile.delete();
            tv.setText("No");
        }

        downloaded = false;
        knn = (EditText) findViewById(R.id.kNN);
        knnParam = (TextView) findViewById(R.id.knnParams);
        svmParam3 = (TextView) findViewById(R.id.svmParams3);
        // Get the split percentage value from text input
        dSplit = (EditText) findViewById(R.id.DSplit);
        dSplit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                dataSplit = Float.valueOf(v.getText().toString());
                Log.d(TAG, "Data Split = "+dataSplit*100+"%");
                return false;
            }
        });

        // Get the type of ML algorithm selected by the user
        algo = (Spinner) findViewById(R.id.ML_spinner);
        algo.setOnItemSelectedListener(new SpinnerActivity());

        svmKernel = (Spinner) findViewById(R.id.SVM_spinner);
        svmKernel.setOnItemSelectedListener(new SpinnerActivity());

        // Read and Create the training - testing split of the data set file

        saveParams = (Button) findViewById(R.id.algoButton);
        // TODO: Need to check the format of traning file we need to send to server

        saveParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Save Params Button Clicked");
                CreateAppFolderIfNeed();
                copyAssetsDataIfNeed();
               try {
                   TextView tv = (TextView) findViewById(R.id.modelName);
                   File modelFile = new File(downloadFilePath+model);
                   if (!modelFile.exists()) {
                       tv.setText("No");
                   }
                   else{
                       tv.setText("Yes");
                   }

                   getDataFromResource(dataSplit, trainFile, testFile);

                   System.out.println(trainData);
                   Toast.makeText(MainActivity.this, "Split Dataset Created", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Cross-Validation Change
        cv = (EditText) findViewById(R.id.cv);
        cv.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                crossValidation = v.getText().toString();
                finalMName = mname+crossValidation;
                if(algorithm==3) {
                    k_nn = v.getText().toString();
                    mname = "KNN_" + k_nn + "_";
                }
                Log.d(TAG, "Cross Validation = "+crossValidation);
                return false;
            }
        });

        knn.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                k_nn = v.getText().toString();
                mname = "KNN_"+k_nn+"_";
                finalMName = mname+crossValidation;
                Log.d(TAG, "k Nearest Neighbor = "+k_nn);
                return false;
            }
        });

        // Training Code
        // Send the data set file to server
        trainButton = (Button) findViewById(R.id.trainbutton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Training Button Clicked");

                //TODO: Need to change the progress update, DEPRECATED
                //TODO Set listener for downloading the model after training data is sent, meanwhile run wait animation on UI thread

                Toast.makeText(MainActivity.this, "Training . . .", Toast.LENGTH_LONG).show();
                long trainStart = System.currentTimeMillis();

                UploadTask uploadTask = new UploadTask(MainActivity.this);
                uploadTask.execute(uploadFilePath + trainFile);

                Log.d(TAG, downloadFilePath+finalMName);

                DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                downloadTask.execute(downloadUrl+finalMName);
                downloaded = true;

                long trainEnd = System.currentTimeMillis();
                long trainDelta = trainEnd - trainStart;
                elapsedTrainSeconds = trainDelta;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                TextView tv = (TextView) findViewById(R.id.modelName);
                File modelFile = new File(downloadFilePath+model);
                System.out.println("file......."+downloadFilePath+model);

                if (!modelFile.exists()) {
                    tv.setText("No");
                    System.out.println("Does not exists..............");
                }
                else{
                    tv.setText("Yes");
                }
                /*
                 else {

                    final ConcurrentHashMap<String, Double> acc = new ConcurrentHashMap<String, Double>();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            DataSource source = new DataSource(trainFile);

                            Instances trainingSet = source.getDataSet();
                            if (trainingSet.classIndex() == -1)
                                trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

                            trainingSet.deleteAttributeAt(0);
                            trainingSet.randomize(new java.util.Random(0));
                            int trainSize = (int) Math.round(trainingSet.numInstances() * 0.8);
                            int testSize = trainingSet.numInstances() - trainSize;
                            Instances train = new Instances(trainingSet, 0, trainSize);
                            Instances test = new Instances(trainingSet, trainSize, testSize);
                            if (mname.startsWith("SVM")) {
                                SMO classifier = new SMO();
                                classifier.buildClassifier(train);
                                weka.core.SerializationHelper.write("svm.model", classifier);
                            } else if (mname.startsWith("LR")){
                                Logistic logistic = new Logistic();
                                logistic.buildClassifier(trainingSet);
                                weka.core.SerializationHelper.write("lr.model", logistic);

                            }else if (mname.startsWith("KNN")){
                                IBk ibk = new IBk(3);
                                ibk.buildClassifier(trainingSet);
                                weka.core.SerializationHelper.write("knn.model", ibk);

                            }else if (mname.startsWith("NB")){
                                NaiveBayes classifier = new NaiveBayes();
                                classifier.buildClassifier(trainingSet);
                                weka.core.SerializationHelper.write("nb.model", classifier);
                            }
                            Evaluation eval = new Evaluation(train);
                            eval.evaluateModel(cls, test);
                            acc.put(Thread.currentThread().getName(),eval.pctCorrect());
                        }
                    };
                    List<Thread> threads = new ArrayList<Thread>();
                    for(int thread_i=0; thread_i < 5; thread_i++){
                        Thread thread = new Thread("Thread_"+ Integer.toString(thread_i));
                        thread.start();
                        threads.start(thread);
                        Log.d("Multi Thread", "Thread Strarted" + thread.getName());
                    }
                    for(Thread thread:threads){
                        try{
                            thread.join();
                        } catch(InterruptedException ex){
                            ex.printStackTrace();
                        }

                    }

                    Double dbl = acc.values().stream().sum();
                    Toast.makeText(MainActivity.this, "Multithreaded Cross Validation Accuracy " + Double.toString(dbl), Toast.LENGTH_LONG).show();
                }
                 */

            }
        });

        // Testing Code

        testButton = (Button) findViewById(R.id.testbutton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Testing Button Clicked");

                try {

                    BufferedReader reader1 = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/"+trainFile));
                    Instances train = new Instances(reader1);
                    reader1.close();
                    train.setClassIndex(train.numAttributes() - 1);

                    BufferedReader reader2 = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/"+testFile));
                    Instances test = new Instances(reader2);
                    reader2.close();
                    test.setClassIndex(test.numAttributes() - 1);

                    File modelFile = new File(downloadFilePath+model);
                    if (!modelFile.exists()) {
                        Toast.makeText(MainActivity.this, "Model file does not exist! Please train model before testing!", Toast.LENGTH_LONG).show();
                    }
                    else {

                        Toast.makeText(MainActivity.this, "Testing . . .", Toast.LENGTH_SHORT).show();

//                        FileInputStream fis = new FileInputStream(downloadFilePath + model);
//                        Classifier cls = (Classifier) weka.core.SerializationHelper.read(fis);
                        Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory() + "/Android/data/MLBenchmark/" + model);
//                    Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmarkTest/abc.model");

                        Evaluation eval = new Evaluation(train);

                        long testStart = System.currentTimeMillis();
                        eval.evaluateModel(cls, test);
                        long testEnd = System.currentTimeMillis();
                        long tDelta = testEnd - testStart;

                        elapsedSeconds = tDelta;



                        Boolean viewIntent = false;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("MST"));
                        String ts = sdf.format(new Date());

                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();

                        String d_Split = Float.toString(dataSplit);
                        bundle.putSerializable("TimeStamp", ts);
                        bundle.putSerializable("DataSplit", d_Split);
                        bundle.putSerializable("ViewIntent", viewIntent);
                        bundle.putSerializable("Model", finalMName);
                        bundle.putSerializable("EvalModel", eval);
                        bundle.putSerializable("TestTime", elapsedSeconds);
                        bundle.putSerializable("TrainTime", elapsedTrainSeconds);
                        intent.putExtras(bundle);
                        intent.setClass(MainActivity.this, testingData.class);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_WRITE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Write to the storage (ex: call appendByteBuffer(byte[] data) here)

                } else {
                    Toast.makeText(getApplicationContext(), "Please grant permission.", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public static void getDataFromResource(float dSplit, String trainFName, String testFName) throws IOException {

        //TODO: Convert breast-cancer-wisconsin.data to breast-cancer-wisconsin.arff

        File myFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.arff");
        FileInputStream fIn = new FileInputStream(myFile);
        BufferedReader r = new BufferedReader( new InputStreamReader( fIn ) );

        Instances trainingSet = new Instances(r);
        if (trainingSet.classIndex() == -1)
            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

        trainingSet.deleteAttributeAt(0);
        trainingSet.randomize(new java.util.Random());

        int trainSize = (int) Math.round(trainingSet.numInstances() * dSplit);
        int testSize = trainingSet.numInstances() - trainSize;
        Instances train = new Instances(trainingSet, 0, trainSize);

        BufferedWriter writer1 = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/"+trainFName));
        writer1.write(train.toString());
        writer1.flush();
        writer1.close();

        Instances test = new Instances(trainingSet, trainSize,testSize);
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/"+testFName));
        writer2.write(test.toString());
        writer2.flush();
        writer2.close();

        return;
    }

    public class SpinnerActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)

            knn.setVisibility(View.INVISIBLE);
            knnParam.setVisibility(View.INVISIBLE);
            svmKernel.setVisibility(View.INVISIBLE);
            svmParam3.setVisibility(View.INVISIBLE);
            switch (parent.getItemAtPosition(pos).toString()){
                case "Logistic Regression": algorithm = 1;
                    model = "lr.model";
                    mname = "LR_";
                    finalMName = mname+crossValidation;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    // get knn param id
                    knn.setVisibility(View.INVISIBLE);
                    knnParam.setVisibility(View.INVISIBLE);
                    svmParam3.setVisibility(View.INVISIBLE);
                    svmKernel.setVisibility(View.INVISIBLE);
                    break;
                case "Náive Bayes Classifier": algorithm = 2;
                    model = "nb.model";
                    mname = "NB_";
                    finalMName = mname+crossValidation;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    knn.setVisibility(View.INVISIBLE);
                    knnParam.setVisibility(View.INVISIBLE);
                    svmParam3.setVisibility(View.INVISIBLE);
                    svmKernel.setVisibility(View.INVISIBLE);
                    break;
                case "k-Nearest Neighbor": algorithm = 3;
                    model = "knn.model";
                    mname = "KNN_"+k_nn+"_";
                    finalMName = mname+crossValidation;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    knn.setVisibility(View.VISIBLE);
                    knnParam.setVisibility(View.VISIBLE);
                    svmParam3.setVisibility(View.INVISIBLE);
                    svmKernel.setVisibility(View.INVISIBLE);
                    break;
                case "Support Vector Machine": algorithm = 4;
                    model = "svm.model";
                    mname = "SVM_poly1_";
                    finalMName = mname+crossValidation;
                    Log.d(TAG,"Called SVM MODEL Selection");
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    knn.setVisibility(View.INVISIBLE);
                    knnParam.setVisibility(View.INVISIBLE);
                    svmParam3.setVisibility(View.VISIBLE);
                    svmKernel.setVisibility(View.VISIBLE);
                    break;
                case "Linear":
                    if(algorithm==4) {
                        svmK = 0;
                        mname = "SVM_poly1_";
                        finalMName = mname + crossValidation;
                        Log.d(TAG, "Called SVM PARAM Selection");
                        if (begin) {
                            svmParam3.setVisibility(View.INVISIBLE);
                            svmKernel.setVisibility(View.INVISIBLE);
                            begin = false;
                        } else {
                            svmParam3.setVisibility(View.VISIBLE);
                            svmKernel.setVisibility(View.VISIBLE);
                            Toast.makeText(parent.getContext(),
                                    "SVM Kernel selected : " + parent.getItemAtPosition(pos).toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case "RBF":
                    if(algorithm==4) {
                        svmK = 1;
                        mname = "SVM_rbf_";
                        finalMName = mname + crossValidation;
                        Toast.makeText(parent.getContext(),
                                "SVM Kernel selected : " + parent.getItemAtPosition(pos).toString(),
                                Toast.LENGTH_SHORT).show();
                        svmParam3.setVisibility(View.VISIBLE);
                        svmKernel.setVisibility(View.VISIBLE);
                    }
                    break;
                case "Polynomial (degree=2)":
                    if(algorithm==4) {
                        svmK = 2;
                        mname = "SVM_poly2_";
                        finalMName = mname + crossValidation;
                        Toast.makeText(parent.getContext(),
                                "SVM Kernel selected : " + parent.getItemAtPosition(pos).toString(),
                                Toast.LENGTH_SHORT).show();
                        svmParam3.setVisibility(View.VISIBLE);
                        svmKernel.setVisibility(View.VISIBLE);
                    }
                    break;
                case "Polynomial (degree=3)":
                    if(algorithm==4) {
                        svmK = 2;
                        mname = "SVM_poly3_";
                        finalMName = mname + crossValidation;
                        Toast.makeText(parent.getContext(),
                                "SVM Kernel selected : " + parent.getItemAtPosition(pos).toString(),
                                Toast.LENGTH_SHORT).show();
                        svmParam3.setVisibility(View.VISIBLE);
                        svmKernel.setVisibility(View.VISIBLE);
                    }
                    break;
                default: Toast.makeText(MainActivity.this, "None", Toast.LENGTH_LONG).show();
            }
            File modelFile = new File(downloadFilePath+model);
            System.out.println("file......."+downloadFilePath+model);
            if (modelFile.exists()) {
                modelFile.delete();
            }
            Log.d(TAG, "Algorithm selected: "+algorithm);
            Log.d(TAG, "SVM selected: "+svmK);

        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Another interface callback
        }
    }

    public class UploadTask extends AsyncTask<String, String, String> {
        private Context context;

        public UploadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection conn = null;
            int serverResponseCode = 0;


            try {

                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(uploadFilePath + trainFile);
                Log.i("File Uri ", sUrl[0]);
                Log.i("File ", sourceFile.toString());

                if (!sourceFile.isFile()) {

                    Log.e("Upload File :", uploadFilePath + trainFile + " does not exist");

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Upload File :" + uploadFilePath + trainFile + " does not exist", Toast.LENGTH_LONG).show();
                        }
                    });

                    return "Source File not exist";

                } else {
                    try {

                        FileInputStream fileInputStream = new FileInputStream(sourceFile);


                        URL url = new URL(upLoadURL);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setUseCaches(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", uploadFilePath + "" + trainFile);


                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=" + "uploaded_file;file="
                                + uploadFilePath + "" + trainFile + "" + lineEnd);

                        dos.writeBytes(lineEnd);

                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        }

                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        conn.connect();
                        serverResponseCode = conn.getResponseCode();
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return "Server returned HTTP " + conn.getResponseCode()
                                    + " " + conn.getResponseMessage();
                        }


                        Log.i("Upload File", "HTTP Response is : "
                                + serverResponseMessage + ": " + serverResponseCode);

                        if (serverResponseCode == 200) {

                            runOnUiThread(new Runnable() {
                                              public void run() {
                                                  Toast.makeText(getApplicationContext(),
                                                          "File Upload Completed", Toast.LENGTH_LONG).show();
                                              }
                                          }

                            );
                        }

                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (MalformedURLException ex) {

                        ex.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "MalformedURLException Exception : check script url.", Toast.LENGTH_LONG).show();
                            }
                        });

                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    } catch (Exception e) {

                        e.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Got Exception : see logcat", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("Upload file to server", "Exception : "
                                + e.getMessage(), e);
                    }
                    return ("String response code:" + serverResponseCode);

                }
            } catch (Exception ex) {
            }
            return "success";
        }


    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();

                input = connection.getInputStream();

                File folder = new File(downloadFilePath);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }

                if (success)
                    output = new FileOutputStream(downloadFilePath + model);
                else {
                    Toast.makeText(getApplicationContext(),
                            "Can not create download directory" + downloadFilePath, Toast.LENGTH_LONG).show();
                }


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                connection.connect();
                while ((count = input.read(data)) != -1) {

                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;

                    output.write(data, 0, count);

                }
                System.out.print("Done");
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Download Completed", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }).start();
                        output.close();
                    }
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            return null;

        }
    }

    private void CreateAppFolderIfNeed(){
        Log.d(TAG, appFolderPath);
        File folder = new File(appFolderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
            if(!success)
                Log.d(TAG,"App folder does not exist");
            else
                Log.d(TAG,"App folder created!");
        } else {
            Log.d(TAG,"App folder exists");
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            assetManager = getAssets();
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, "[ERROR]: copyAsset: unable to copy file = "+fromAssetPath);
            return false;
        }
    }

    private void copyAssetsDataIfNeed(){
        String assetsToCopy[] = {"breast-cancer-wisconsin.arff"};
        for(int i=0; i<assetsToCopy.length; i++){
            String from = assetsToCopy[i];
            String to = appFolderPath+from;

            // 1. check if file exist
            File file = new File(to);
            if(file.exists()){
                Log.d(TAG, "copyAssetsDataIfNeed: file exist, no need to copy:"+from);
            } else {
                // do copy
                boolean copyResult = copyAsset(getAssets(), from, to);
                Log.d(TAG, "copyAssetsDataIfNeed: copy result = " + copyResult + " of file = " + from);
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
